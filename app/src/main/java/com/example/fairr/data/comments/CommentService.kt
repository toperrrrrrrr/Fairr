package com.example.fairr.data.comments

import com.example.fairr.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Get comments for an expense as a Flow
     */
    fun getCommentsForExpense(expenseId: String): Flow<List<Comment>> = flow {
        try {
            val commentsRef = firestore.collection("expenses")
                .document(expenseId)
                .collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            
            val snapshot = commentsRef.get().await()
            val comments = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Comment(
                        id = doc.id,
                        expenseId = expenseId,
                        authorId = data["authorId"] as? String ?: "",
                        authorName = data["authorName"] as? String ?: "",
                        text = data["text"] as? String ?: "",
                        timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                        editedAt = data["editedAt"] as? Timestamp
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(comments)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Add a new comment to an expense
     */
    suspend fun addComment(expenseId: String, text: String): Result<Comment> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User must be authenticated"))
            
            // Get user display name
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            
            val authorName = userDoc.getString("displayName") ?: currentUser.email?.substringBefore("@") ?: "Unknown User"
            
            val commentData = HashMap<String, Any>()
            commentData["authorId"] = currentUser.uid
            commentData["authorName"] = authorName
            commentData["text"] = text
            commentData["timestamp"] = Timestamp.now()
            
            val commentRef = firestore.collection("expenses")
                .document(expenseId)
                .collection("comments")
                .add(commentData)
                .await()
            
            val comment = Comment(
                id = commentRef.id,
                expenseId = expenseId,
                authorId = currentUser.uid,
                authorName = authorName,
                text = text,
                timestamp = Timestamp.now()
            )
            
            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update an existing comment
     */
    suspend fun updateComment(expenseId: String, commentId: String, newText: String): Result<Comment> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User must be authenticated"))
            
            // Verify the comment exists and belongs to the current user
            val commentDoc = firestore.collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()
            
            if (!commentDoc.exists()) {
                return Result.failure(Exception("Comment not found"))
            }
            
            val authorId = commentDoc.getString("authorId")
            if (authorId != currentUser.uid) {
                return Result.failure(Exception("You can only edit your own comments"))
            }
            
            // Update the comment
            val updateData = HashMap<String, Any>()
            updateData["text"] = newText
            updateData["editedAt"] = Timestamp.now()
            
            commentDoc.reference.update(updateData).await()
            
            // Return updated comment
            val updatedComment = Comment(
                id = commentId,
                expenseId = expenseId,
                authorId = authorId,
                authorName = commentDoc.getString("authorName") ?: "",
                text = newText,
                timestamp = commentDoc.getTimestamp("timestamp") ?: Timestamp.now(),
                editedAt = Timestamp.now()
            )
            
            Result.success(updatedComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a comment
     */
    suspend fun deleteComment(expenseId: String, commentId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User must be authenticated"))
            
            // Verify the comment exists and belongs to the current user
            val commentDoc = firestore.collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()
            
            if (!commentDoc.exists()) {
                return Result.failure(Exception("Comment not found"))
            }
            
            val authorId = commentDoc.getString("authorId")
            if (authorId != currentUser.uid) {
                return Result.failure(Exception("You can only delete your own comments"))
            }
            
            // Delete the comment
            commentDoc.reference.delete().await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 