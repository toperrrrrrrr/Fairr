package com.example.fairr.di

import com.example.fairr.data.repository.GroupRepository
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.UserRepository
import com.example.fairr.data.repository.ExpenseRepositoryImpl
import com.example.fairr.data.repository.GroupRepositoryImpl
import com.example.fairr.data.repository.UserRepositoryImpl
import com.example.fairr.data.activity.ActivityService
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.notifications.NotificationService
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.friends.FriendGroupService
import com.example.fairr.data.notifications.RecurringExpenseNotificationService
import com.example.fairr.data.export.ExportService
import com.example.fairr.data.analytics.AnalyticsService
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.data.preferences.UserPreferencesManager
import com.example.fairr.utils.PhotoUtils
import com.example.fairr.util.PerformanceOptimizer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.fairr.data.comments.CommentService
import com.example.fairr.data.category.CategoryService
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
    ): GroupRepository = GroupRepositoryImpl(auth, firestore)

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
        activityService: ActivityService,
        notificationService: NotificationService
    ): ExpenseRepository = ExpenseRepositoryImpl(firestore, auth, activityService, notificationService)

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
        auth: FirebaseAuth,
        notificationService: NotificationService
    ): GroupInviteService = GroupInviteService(firestore, auth, notificationService)

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
        groupRepository: GroupRepository,
        auth: FirebaseAuth
    ): RecurringExpenseNotificationService = RecurringExpenseNotificationService(context, expenseRepository, groupRepository, auth)

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
    ): com.example.fairr.data.user.UserModerationService = com.example.fairr.data.user.UserModerationService(auth, firestore)

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
    ): com.example.fairr.data.notifications.SimpleNotificationService = com.example.fairr.data.notifications.SimpleNotificationService(context)

    @Provides
    @Singleton
    fun provideFirebaseStorage(): com.google.firebase.storage.FirebaseStorage = com.google.firebase.storage.FirebaseStorage.getInstance()

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