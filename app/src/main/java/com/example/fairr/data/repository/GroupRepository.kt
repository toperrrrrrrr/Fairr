package com.example.fairr.data.repository

import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.model.GroupRole
import com.example.fairr.ui.model.CreateGroupData
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing group data operations.
 * 
 * This repository handles all group-related data operations including:
 * - CRUD operations for groups
 * - Member management and role assignments
 * - Real-time data synchronization with Firebase
 * - Group archiving and lifecycle management
 * - Invitation and joining workflows
 * 
 * Threading: All suspend functions are safe to call from any coroutine context.
 * They will automatically switch to appropriate dispatchers as needed.
 * 
 * Error Handling: All methods may throw [FirebaseException] for network issues,
 * [AuthenticationException] for auth failures, or [ValidationException] for
 * invalid input data.
 * 
 * @since 1.0.0
 * @author Fairr Development Team
 */
interface GroupRepository {
    
    /**
     * Get all groups for the current user
     * @return Flow of groups that updates in real-time
     */
    fun getUserGroups(): Flow<List<Group>>
    
    /**
     * Get active (non-archived) groups for the current user
     * @return Flow of active groups
     */
    fun getActiveGroups(): Flow<List<Group>>
    
    /**
     * Get archived groups for the current user
     * @return Flow of archived groups
     */
    fun getArchivedGroups(): Flow<List<Group>>
    
    /**
     * Get a specific group by ID
     * @param groupId The ID of the group to retrieve
     * @return Flow of the group that updates in real-time
     */
    fun getGroup(groupId: String): Flow<Group>
    
    /**
     * Create a new group
     * @param data The group creation data
     * @return Result indicating success or failure
     */
    suspend fun createGroup(data: CreateGroupData): GroupResult
    
    /**
     * Join a group using invite code
     * @param groupId The ID of the group to join
     * @return Result indicating success or failure
     */
    suspend fun joinGroup(groupId: String): GroupResult
    
    /**
     * Leave a group
     * @param groupId The ID of the group to leave
     * @return Result indicating success or failure
     */
    suspend fun leaveGroup(groupId: String): GroupResult
    
    /**
     * Delete a group (admin only)
     * @param groupId The ID of the group to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteGroup(groupId: String): GroupResult
    
    /**
     * Update group information (admin only)
     * @param groupId The ID of the group to update
     * @param name New group name
     * @param description New group description
     * @param currency New group currency
     * @return Result indicating success or failure
     */
    suspend fun updateGroup(
        groupId: String,
        name: String,
        description: String,
        currency: String
    ): GroupResult
    
    /**
     * Remove a member from the group (admin only)
     * @param groupId The ID of the group
     * @param userId The ID of the user to remove
     * @return Result indicating success or failure
     */
    suspend fun removeMember(groupId: String, userId: String): GroupResult
    
    /**
     * Promote a member to admin (admin only)
     * @param groupId The ID of the group
     * @param userId The ID of the user to promote
     * @return Result indicating success or failure
     */
    suspend fun promoteToAdmin(groupId: String, userId: String): GroupResult
    
    /**
     * Demote an admin to member (admin only)
     * @param groupId The ID of the group
     * @param userId The ID of the user to demote
     * @return Result indicating success or failure
     */
    suspend fun demoteFromAdmin(groupId: String, userId: String): GroupResult
    
    /**
     * Archive a group (admin only)
     * @param groupId The ID of the group to archive
     * @return Result indicating success or failure
     */
    suspend fun archiveGroup(groupId: String): GroupResult
    
    /**
     * Unarchive a group (admin only)
     * @param groupId The ID of the group to unarchive
     * @return Result indicating success or failure
     */
    suspend fun unarchiveGroup(groupId: String): GroupResult
}

/**
 * Result wrapper for group operations
 */
sealed class GroupResult {
    data class Success(val groupId: String) : GroupResult()
    data class Error(val message: String) : GroupResult()
} 