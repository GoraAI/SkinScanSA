package com.skinscan.sa.di

import com.skinscan.sa.data.repository.SkinAnalysisRepositoryImpl
import com.skinscan.sa.domain.repository.SkinAnalysisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository bindings
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSkinAnalysisRepository(
        impl: SkinAnalysisRepositoryImpl
    ): SkinAnalysisRepository
}
