package com.example.fairr.data.comments

import com.example.fairr.data.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.ktx.toObjects

@Singleton
class CommentService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Get comments for an expense
     */
    fun getCommentsForExpense(expenseId: String): Flow<List<Comment>> = flow {
        try {
            val querySnapshot = firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
                
            val comments = querySnapshot.documents.mapNotNull { doc ->
                doc.toObject<Comment>()?.copy(id = doc.id)
            }
            
            emit(comments)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Add a new comment to an expense
     */
    suspend fun addComment(
        expenseId: String,
        groupId: String,
        text: String,
        authorName: String,
        authorPhotoUrl: String = ""
    ): Result<Comment> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            
            val comment = Comment(
                expenseId = expenseId,
                authorId = currentUser.uid,
                authorName = authorName,
                authorPhotoUrl = authorPhotoUrl,
                text = text.trim(),
                timestamp = Timestamp.now(),
                groupId = groupId
            )
            
            val documentRef = firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .add(comment)
                .await()
                
            val savedComment = comment.copy(id = documentRef.id)
            Result.success(savedComment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Edit an existing comment
     */
    suspend fun editComment(
        expenseId: String,
        commentId: String,
        newText: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            
            // First check if the user owns this comment
            val commentDoc = firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()
                
            val comment = commentDoc.toObject<Comment>()
            if (comment?.authorId != currentUser.uid) {
                return Result.failure(Exception("You can only edit your own comments"))
            }
            
            // Update the comment
            firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .update(
                    mapOf(
                        "text" to newText.trim(),
                        "isEdited" to true,
                        "editedAt" to Timestamp.now()
                    )
                )
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a comment
     */
    suspend fun deleteComment(
        expenseId: String,
        commentId: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            
            // First check if the user owns this comment
            val commentDoc = firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .get()
                .await()
                
            val comment = commentDoc.toObject<Comment>()
            if (comment?.authorId != currentUser.uid) {
                return Result.failure(Exception("You can only delete your own comments"))
            }
            
            // Delete the comment
            firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .document(commentId)
                .delete()
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get comment count for an expense
     */
    suspend fun getCommentCount(expenseId: String): Int {
        return try {
            val querySnapshot = firestore
                .collection("expenses")
                .document(expenseId)
                .collection("comments")
                .get()
                .await()
            
            querySnapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    private val commentsCollection = firestore.collection("comments")

    suspend fun getComment(commentId: String): Comment? {
        return commentsCollection.document(commentId)
            .get()
            .await()
            .toObject<Comment>()
    }

    suspend fun getComments(expenseId: String): List<Comment> {
        return commentsCollection
            .whereEqualTo("expenseId", expenseId)
            .get()
            .await()
            .toObjects()
    }

    suspend fun getGroupComments(groupId: String): List<Comment> {
        return commentsCollection
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
            .toObjects()
    }

    suspend fun getLatestComments(groupId: String, limit: Int = 10): List<Comment> {
        return commentsCollection
            .whereEqualTo("groupId", groupId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
            .toObjects()
    }

    suspend fun getCommentsByUser(userId: String): List<Comment> {
        return commentsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects()
    }
} 