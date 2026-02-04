package com.sangyoon.vehiclenote.di

import android.content.Context
import androidx.room.Room
import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.local.database.VehicleDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVehicleDatabase(
        @ApplicationContext context: Context
    ): VehicleDatabase {
        return Room.databaseBuilder(
            context,
            VehicleDatabase::class.java,
            "vehicle_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: VehicleDatabase): VehicleDao {
        return database.vehicleDao()
    }
}