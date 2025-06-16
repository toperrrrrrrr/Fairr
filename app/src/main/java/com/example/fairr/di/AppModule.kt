package com.example.fairr.di

import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.notifications.NotificationService
import com.example.fairr.data.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

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
} 