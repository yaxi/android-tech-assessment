package com.pelagohealth.codingchallenge.domain.di

import com.pelagohealth.codingchallenge.domain.CoroutineDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CoroutineDispatcherModule {

    @Singleton
    @Provides
    fun provideCoroutineDispatchers() = CoroutineDispatchers(
        io = Dispatchers.IO,
        default = Dispatchers.Default,
        main = Dispatchers.Main
    )
}