package com.example.fairr.data.friends

import android.util.Log
import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.model.FriendRequest
import com.example.fairr.ui.model.FriendStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FriendService"

sealed class FriendResult {
    data class Success(val message: String) : FriendResult()
    data class Error(val message: String) : FriendResult()
}

@Singleton
class FriendService @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val friendsCollection = firestore.collection("friends")
    private val friendRequestsCollection = firestore.collection("friendRequests")

    fun getUserFriends(): Flow<List<Friend>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        // Query for friends where user is either userId or friendId
        val subscription = friendsCollection
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching friends as userId", error)
                    close(error)
                    return@addSnapshotListener
                }

                // Get friends where user is userId
                val friendsAsUser = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Friend(
                            id = doc.getString("friendId") ?: return@mapNotNull null,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "",
                            photoUrl = doc.getString("photoUrl"),
                            status = FriendStatus.valueOf(doc.getString("status") ?: FriendStatus.PENDING.name),
                            addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing friend document", e)
                        null
                    }
                } ?: emptyList()

                // Query for friends where user is friendId
                friendsCollection
                    .whereEqualTo("friendId", currentUser.uid)
                    .get()
                    .addOnSuccessListener { friendIdSnapshot ->
                        val friendsAsFriend = friendIdSnapshot.documents.mapNotNull { doc ->
                            try {
                                Friend(
                                    id = doc.getString("userId") ?: return@mapNotNull null,
                                    name = doc.getString("name") ?: "Unknown",
                                    email = doc.getString("email") ?: "",
                                    photoUrl = doc.getString("photoUrl"),
                                    status = FriendStatus.valueOf(doc.getString("status") ?: FriendStatus.PENDING.name),
                                    addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing friend document", e)
                                null
                            }
                        }

                        // Combine both lists
                        val allFriends = (friendsAsUser + friendsAsFriend).distinctBy { it.id }
                        Log.d(TAG, "Processed ${allFriends.size} friends")
                        trySend(allFriends)
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error fetching friends as friendId", e)
                        trySend(friendsAsUser) // Still send the first list if second query fails
                    }
            }

        awaitClose { subscription.remove() }
    }

    fun getPendingFriendRequests(): Flow<List<FriendRequest>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        val subscription = friendRequestsCollection
            .whereEqualTo("receiverId", currentUser.uid)
            .whereEqualTo("status", FriendStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching friend requests", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.d(TAG, "No friend requests found (snapshot is null)")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val requests = snapshot.documents.mapNotNull { doc ->
                    try {
                        FriendRequest(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: return@mapNotNull null,
                            senderName = doc.getString("senderName") ?: "Unknown",
                            senderEmail = doc.getString("senderEmail") ?: "",
                            senderPhotoUrl = doc.getString("senderPhotoUrl"),
                            receiverId = doc.getString("receiverId") ?: return@mapNotNull null,
                            status = FriendStatus.valueOf(doc.getString("status") ?: FriendStatus.PENDING.name),
                            sentAt = doc.getLong("sentAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing friend request document", e)
                        null
                    }
                }

                Log.d(TAG, "Processed ${requests.size} friend requests")
                trySend(requests)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun sendFriendRequest(email: String): FriendResult {
        try {
            val currentUser = auth.currentUser
                ?: return FriendResult.Error("User not authenticated")

            // Check if user is trying to add themselves
            if (email == currentUser.email) {
                return FriendResult.Error("You cannot add yourself as a friend")
            }

            // Find user by email
            val userQuery = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (userQuery.isEmpty) {
                return FriendResult.Error("User not found")
            }

            val receiverData = userQuery.documents.first()
            val receiverId = receiverData.id

            // Create a deterministic document ID
            val requestId = "${currentUser.uid}_${receiverId}"

            // Check if friend request already exists
            val existingRequest = friendRequestsCollection.document(requestId).get().await()
            if (existingRequest.exists()) {
                return FriendResult.Error("Friend request already sent")
            }

            // Check if they're already friends
            val existingFriend = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendId", receiverId)
                .get()
                .await()

            if (!existingFriend.isEmpty) {
                return FriendResult.Error("You are already friends with this user")
            }

            // Create friend request with specific document ID
            val friendRequest = hashMapOf(
                "senderId" to currentUser.uid,
                "senderName" to (currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown"),
                "senderEmail" to currentUser.email,
                "senderPhotoUrl" to currentUser.photoUrl?.toString(),
                "receiverId" to receiverId,
                "status" to FriendStatus.PENDING.name,
                "sentAt" to System.currentTimeMillis()
            )

            // Add debug log
            Log.d(TAG, "Sending friend request: $friendRequest with ID: $requestId")

            // Use set() with the specific document ID instead of add()
            friendRequestsCollection.document(requestId).set(friendRequest).await()
            return FriendResult.Success("Friend request sent successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error sending friend request", e)
            return FriendResult.Error(e.message ?: "Failed to send friend request")
        }
    }

    suspend fun acceptFriendRequest(requestId: String): FriendResult {
        try {
            val currentUser = auth.currentUser
                ?: return FriendResult.Error("User not authenticated")

            val requestDoc = friendRequestsCollection.document(requestId).get().await()
            if (!requestDoc.exists()) {
                return FriendResult.Error("Friend request not found")
            }

            val senderId = requestDoc.getString("senderId")
                ?: return FriendResult.Error("Invalid friend request data")

            // Create deterministic document IDs for friend entries
            val currentUserFriendId = "${currentUser.uid}_${senderId}"
            val senderFriendId = "${senderId}_${currentUser.uid}"

            // Create friend entries for both users
            val currentUserFriend = hashMapOf(
                "userId" to currentUser.uid,
                "friendId" to senderId,
                "name" to requestDoc.getString("senderName"),
                "email" to requestDoc.getString("senderEmail"),
                "photoUrl" to requestDoc.getString("senderPhotoUrl"),
                "status" to FriendStatus.ACCEPTED.name,
                "addedAt" to System.currentTimeMillis()
            )

            val senderFriend = hashMapOf(
                "userId" to senderId,
                "friendId" to currentUser.uid,
                "name" to (currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown"),
                "email" to currentUser.email,
                "photoUrl" to currentUser.photoUrl?.toString(),
                "status" to FriendStatus.ACCEPTED.name,
                "addedAt" to System.currentTimeMillis()
            )

            firestore.runBatch { batch ->
                // Use deterministic document IDs for friend entries
                batch.set(friendsCollection.document(currentUserFriendId), currentUserFriend)
                batch.set(friendsCollection.document(senderFriendId), senderFriend)
                batch.update(
                    friendRequestsCollection.document(requestId),
                    "status", FriendStatus.ACCEPTED.name
                )
            }.await()

            return FriendResult.Success("Friend request accepted")

        } catch (e: Exception) {
            Log.e(TAG, "Error accepting friend request", e)
            return FriendResult.Error(e.message ?: "Failed to accept friend request")
        }
    }

    suspend fun rejectFriendRequest(requestId: String): FriendResult {
        try {
            val currentUser = auth.currentUser
                ?: return FriendResult.Error("User not authenticated")

            val requestDoc = friendRequestsCollection.document(requestId)
            val request = requestDoc.get().await()

            if (!request.exists()) {
                return FriendResult.Error("Friend request not found")
            }

            if (request.getString("receiverId") != currentUser.uid) {
                return FriendResult.Error("Unauthorized to reject this request")
            }

            requestDoc.delete().await()
            return FriendResult.Success("Friend request rejected")

        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting friend request", e)
            return FriendResult.Error(e.message ?: "Failed to reject friend request")
        }
    }

    suspend fun removeFriend(friendId: String): FriendResult {
        try {
            val currentUser = auth.currentUser
                ?: return FriendResult.Error("User not authenticated")

            // Delete both friend entries
            val batch = firestore.batch()

            val currentUserFriend = friendsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("friendId", friendId)
                .get()
                .await()

            val otherUserFriend = friendsCollection
                .whereEqualTo("userId", friendId)
                .whereEqualTo("friendId", currentUser.uid)
                .get()
                .await()

            if (currentUserFriend.isEmpty && otherUserFriend.isEmpty) {
                return FriendResult.Error("Friend relationship not found")
            }

            currentUserFriend.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            otherUserFriend.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            return FriendResult.Success("Friend removed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error removing friend", e)
            return FriendResult.Error(e.message ?: "Failed to remove friend")
        }
    }
} 