package com.example.fairr.data.user

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserProfileService"

data class UserProfileUpdate(
    val displayName: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

sealed class ProfileUpdateResult {
    object Success : ProfileUpdateResult()
    data class Error(val message: String) : ProfileUpdateResult()
    object Loading : ProfileUpdateResult()
}

@Singleton
class UserProfileService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")
    
    /**
     * Update user profile information
     */
    suspend fun updateUserProfile(update: UserProfileUpdate): ProfileUpdateResult {
        return try {
            val currentUser = auth.currentUser ?: return ProfileUpdateResult.Error("User not authenticated")
            
            Log.d(TAG, "Updating user profile for: ${currentUser.uid}")
            
            // Prepare Firebase Auth profile update
            val profileUpdates = UserProfileChangeRequest.Builder()
            update.displayName?.let { profileUpdates.setDisplayName(it) }
            update.photoUrl?.let { profileUpdates.setPhotoUri(Uri.parse(it)) }
            
            // Update Firebase Auth profile
            currentUser.updateProfile(profileUpdates.build()).await()
            
            // Prepare Firestore update
            val firestoreUpdates = mutableMapOf<String, Any>(
                "lastUpdatedAt" to System.currentTimeMillis()
            )
            
            update.displayName?.let { firestoreUpdates["displayName"] = it }
            update.phoneNumber?.let { firestoreUpdates["phoneNumber"] = it }
            update.photoUrl?.let { firestoreUpdates["photoUrl"] = it }
            
            // Update Firestore document
            usersCollection.document(currentUser.uid)
                .update(firestoreUpdates)
                .await()
            
            Log.d(TAG, "Profile updated successfully")
            ProfileUpdateResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            ProfileUpdateResult.Error(e.message ?: "Failed to update profile")
        }
    }
    
    /**
     * Upload profile image to Firebase Storage
     */
    suspend fun uploadProfileImage(imageUri: Uri): ProfileUpdateResult {
        return try {
            val currentUser = auth.currentUser ?: return ProfileUpdateResult.Error("User not authenticated")
            
            Log.d(TAG, "Uploading profile image for user: ${currentUser.uid}")
            
            // Create a reference to store the image
            val imageRef = storage.reference
                .child("profile_images")
                .child(currentUser.uid)
                .child("profile_${System.currentTimeMillis()}.jpg")
            
            // Upload the image
            val uploadTask = imageRef.putFile(imageUri).await()
            
            // Get the download URL
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            
            // Update profile with new image URL
            updateUserProfile(UserProfileUpdate(photoUrl = downloadUrl.toString()))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image", e)
            ProfileUpdateResult.Error(e.message ?: "Failed to upload image")
        }
    }
    
    /**
     * Update email address (requires re-authentication)
     */
    suspend fun updateEmail(newEmail: String): ProfileUpdateResult {
        return try {
            val currentUser = auth.currentUser ?: return ProfileUpdateResult.Error("User not authenticated")
            
            Log.d(TAG, "Updating email for user: ${currentUser.uid}")
            
            // Update email in Firebase Auth
            currentUser.updateEmail(newEmail).await()
            
            // Update email in Firestore
            usersCollection.document(currentUser.uid)
                .update(mapOf(
                    "email" to newEmail,
                    "lastUpdatedAt" to System.currentTimeMillis()
                ))
                .await()
            
            Log.d(TAG, "Email updated successfully")
            ProfileUpdateResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "Error updating email", e)
            ProfileUpdateResult.Error(e.message ?: "Failed to update email")
        }
    }
    
    /**
     * Get current user profile data
     */
    suspend fun getCurrentUserProfile(): UserProfileData? {
        return try {
            val currentUser = auth.currentUser ?: return null
            
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            
            UserProfileData(
                uid = currentUser.uid,
                displayName = currentUser.displayName ?: userDoc.getString("displayName"),
                email = currentUser.email ?: userDoc.getString("email"),
                phoneNumber = userDoc.getString("phoneNumber"),
                photoUrl = currentUser.photoUrl?.toString() ?: userDoc.getString("photoUrl"),
                isEmailVerified = currentUser.isEmailVerified,
                joinDate = userDoc.getLong("createdAt"),
                lastUpdated = userDoc.getLong("lastUpdatedAt")
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }
    
    /**
     * Validate profile data before update
     */
    fun validateProfileUpdate(update: UserProfileUpdate): String? {
        // Validate display name
        update.displayName?.let { name ->
            if (name.isBlank()) {
                return "Display name cannot be empty"
            }
            if (name.length < 2) {
                return "Display name must be at least 2 characters"
            }
            if (name.length > 50) {
                return "Display name must be less than 50 characters"
            }
        }
        
        // Validate phone number
        update.phoneNumber?.let { phone ->
            if (phone.isNotBlank() && !isValidPhoneNumber(phone)) {
                return "Please enter a valid phone number"
            }
        }
        
        // Validate email
        update.email?.let { email ->
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return "Please enter a valid email address"
            }
        }
        
        return null
    }
    
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation - you can make this more sophisticated
        val phoneRegex = Regex("^[+]?[1-9]\\d{1,14}$")
        val cleanedPhone = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
        return phoneRegex.matches(cleanedPhone)
    }
}

data class UserProfileData(
    val uid: String,
    val displayName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val joinDate: Long? = null,
    val lastUpdated: Long? = null
) 