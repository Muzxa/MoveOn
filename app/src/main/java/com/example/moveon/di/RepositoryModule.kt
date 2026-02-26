package com.example.moveon.di

import com.example.moveon.data.repository.AuthRepositoryImpl
import com.example.moveon.data.repository.InventoryRepositoryImpl
import com.example.moveon.data.repository.LogisticsRepositoryImpl
import com.example.moveon.domain.repository.AuthRepository
import com.example.moveon.domain.repository.InventoryRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @Singleton
    abstract fun bindLogisticsRepository(impl: LogisticsRepositoryImpl): LogisticsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl) : AuthRepository
}