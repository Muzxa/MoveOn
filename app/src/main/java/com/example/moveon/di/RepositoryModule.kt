package com.example.moveon.di

import com.example.moveon.data.repository.InventoryRepositoryImpl
import com.example.moveon.data.repository.LogisticsRepositoryImpl
import com.example.moveon.domain.repository.InventoryRepository
import com.example.moveon.domain.repository.LogisticsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @ViewModelScoped
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds
    @ViewModelScoped
    abstract fun bindLogisticsRepository(impl: LogisticsRepositoryImpl): LogisticsRepository
}