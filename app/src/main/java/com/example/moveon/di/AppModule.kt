package com.example.moveon.di

import android.content.Context
import androidx.room.Room
import com.example.moveon.data.local.MoveOnDatabase
import com.example.moveon.data.local.dao.UserPreferences
import com.example.moveon.data.local.dao.UserPreferencesImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMoveOnDatabase(@ApplicationContext context: Context): MoveOnDatabase {
        return Room.databaseBuilder(
            context,
            MoveOnDatabase::class.java,
            "moveon_database"
        )
            .addMigrations(MoveOnDatabase.MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideBoxDao(database: MoveOnDatabase) = database.boxDao()

    @Provides
    fun provideItemDao(database: MoveOnDatabase) = database.itemDao()

    @Provides
    fun provideUserSessionDao(database: MoveOnDatabase) = database.userSessionDao()

    @Provides
    @Singleton
    fun provideUserPreferences(userPreferencesImpl: UserPreferencesImpl): UserPreferences =
        userPreferencesImpl

    @Provides
    @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideRealtimeDatabase() = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()
}
