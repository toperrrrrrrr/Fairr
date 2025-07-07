package com.example.fairr.di

import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.notifications.NotificationService
import com.example.fairr.data.notifications.RecurringExpenseNotificationService
import com.example.fairr.data.activity.ActivityService
import com.example.fairr.data.repository.*
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.data.user.UserModerationService
import com.example.fairr.data.friends.FriendGroupService
import com.example.fairr.data.comments.CommentService
import com.example.fairr.data.category.CategoryService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.example.fairr.utils.PhotoUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.fairr.data.notifications.SimpleNotificationService
import com.google.firebase.Firebase
import com.google.firebase.firestore.MemoryCacheSettings

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        return FirebaseFirestore.getInstance().apply {
            firestoreSettings = settings
        }
    }

    @Provides
    @Singleton
    fun provideGroupService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): GroupService = GroupService(auth, firestore)

    @Provides
    @Singleton
    fun provideActivityService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): ActivityService = ActivityService(firestore, auth)

    @Provides
    @Singleton
    fun provideExpenseRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        activityService: ActivityService
    ): ExpenseRepository = ExpenseRepositoryImpl(firestore, auth, activityService)

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
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): GroupInviteService = GroupInviteService(firestore, auth)

    @Provides
    @Singleton
    fun provideSettlementService(
        expenseRepository: ExpenseRepository,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): com.example.fairr.data.settlements.SettlementService = com.example.fairr.data.settlements.SettlementService(expenseRepository, firestore, auth)

    @Provides
    @Singleton
    fun provideRecurringExpenseNotificationService(
        context: Context,
        expenseRepository: ExpenseRepository,
        groupService: GroupService,
        auth: FirebaseAuth
    ): RecurringExpenseNotificationService = RecurringExpenseNotificationService(context, expenseRepository, groupService, auth)

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore = SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideUserModerationService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): UserModerationService = UserModerationService(auth, firestore)

    @Provides
    @Singleton
    fun provideFriendGroupService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): FriendGroupService = FriendGroupService(auth, firestore)

    @Provides
    @Singleton
    fun provideCommentService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CommentService = CommentService(firestore, auth)

    @Provides
    @Singleton
    fun provideCategoryService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CategoryService = CategoryService(firestore, auth)

    @Provides
    @Singleton
    fun provideSimpleNotificationService(
        @ApplicationContext context: Context
    ): SimpleNotificationService = SimpleNotificationService(context)

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun providePhotoUtils(): PhotoUtils = PhotoUtils

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics = 
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideFirebasePerformance(): FirebasePerformance = FirebasePerformance.getInstance()
} 