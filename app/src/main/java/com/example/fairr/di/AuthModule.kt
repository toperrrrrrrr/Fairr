package com.example.fairr.di

import android.content.Context
import com.example.fairr.data.auth.AuthService
import com.example.fairr.data.auth.GoogleAuthService
import com.example.fairr.data.repository.UserRepository
import com.example.fairr.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideAuthService(auth: FirebaseAuth): AuthService {
        return AuthService(auth)
    }

    @Provides
    @Singleton
    fun provideUserRepository(firestore: FirebaseFirestore): UserRepository {
        return UserRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthService(
        auth: FirebaseAuth,
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): GoogleAuthService {
        return GoogleAuthService(auth, context, userRepository)
    }
} 