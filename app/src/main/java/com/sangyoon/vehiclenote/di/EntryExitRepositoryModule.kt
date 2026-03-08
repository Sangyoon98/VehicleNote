package com.sangyoon.vehiclenote.di

import com.sangyoon.vehiclenote.data.repository.EntryExitRepositoryImpl
import com.sangyoon.vehiclenote.domain.repository.EntryExitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EntryExitRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryExitRepository(
        impl: EntryExitRepositoryImpl
    ): EntryExitRepository
}
