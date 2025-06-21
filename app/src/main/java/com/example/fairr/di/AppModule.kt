package com.example.fairr.di

import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.notifications.NotificationService
import com.example.fairr.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideGroupService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): GroupService = GroupService(auth, firestore)

    @Provides
    @Singleton
    fun provideExpenseRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ExpenseRepository = ExpenseRepositoryImpl(firestore, auth)

    @Provides
    @Singleton
    fun provideGroupJoinService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): GroupJoinService = GroupJoinService(auth, firestore)

    @Provides
    @Singleton
    fun provideNotificationService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): NotificationService = NotificationService(auth, firestore)

    @Provides
    @Singleton
    fun provideGroupInviteService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): GroupInviteService = GroupInviteService(auth, firestore)

    @Provides
    @Singleton
    fun provideSettlementService(
        expenseRepository: ExpenseRepository,
        firestore: FirebaseFirestore
    ): com.example.fairr.data.settlements.SettlementService = com.example.fairr.data.settlements.SettlementService(expenseRepository, firestore)
} 