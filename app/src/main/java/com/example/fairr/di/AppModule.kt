package com.example.fairr.di

import com.example.fairr.data.groups.GroupService
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
    fun provideGroupService(): GroupService {
        return GroupService()
    }
} 