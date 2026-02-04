package com.sangyoon.vehiclenote.di

import com.sangyoon.vehiclenote.data.local.dao.VehicleDao
import com.sangyoon.vehiclenote.data.repository.VehicleRepositoryImpl
import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideVehicleRepository(
        vehicleDao: VehicleDao
    ): VehicleRepository {
        return VehicleRepositoryImpl(vehicleDao)
    }
}