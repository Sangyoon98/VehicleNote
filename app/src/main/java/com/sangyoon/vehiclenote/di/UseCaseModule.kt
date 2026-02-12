package com.sangyoon.vehiclenote.di

import com.sangyoon.vehiclenote.domain.repository.VehicleRepository
import com.sangyoon.vehiclenote.domain.usecase.AddVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.DeleteVehicleUseCase
import com.sangyoon.vehiclenote.domain.usecase.GetAllVehiclesUseCase
import com.sangyoon.vehiclenote.domain.usecase.SearchVehicleUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetAllVehiclesUseCase(
        repository: VehicleRepository
    ): GetAllVehiclesUseCase {
        return GetAllVehiclesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddVehicleUseCase(
        repository: VehicleRepository
    ): AddVehicleUseCase {
        return AddVehicleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchVehicleUseCase(
        repository: VehicleRepository
    ): SearchVehicleUseCase {
        return SearchVehicleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteVehicleUseCase(
        repository: VehicleRepository
    ): DeleteVehicleUseCase {
        return DeleteVehicleUseCase(repository)
    }
}