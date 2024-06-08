package com.pelagohealth.codingchallenge.data

import com.pelagohealth.codingchallenge.BuildConfig
import com.pelagohealth.codingchallenge.data.datasource.rest.FactsRestApi
import com.pelagohealth.codingchallenge.data.mapper.FactMapper
import com.pelagohealth.codingchallenge.data.repository.FactRepository
import com.pelagohealth.codingchallenge.domain.util.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Hilt module that provides dependencies for the data layer.
 */
@InstallIn(SingletonComponent::class)
@Module
class DataModule {

    // Base URL for the REST API: https://uselessfacts.jsph.pl/api/v2

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? {
        if (!BuildConfig.DEBUG) {
            return null
        }

        return HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor?): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (loggingInterceptor != null) {
                    addInterceptor(loggingInterceptor)
                }
            }
            .build()
    }

    @Singleton
    @Provides
    fun provideMoshi() = Moshi
        .Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Singleton
    @Provides
    fun provideFactsRestApi(client: OkHttpClient, moshi: Moshi): FactsRestApi {
        val baseUrl = Constants.Network.BASE_URL
        return Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(baseUrl)
            .client(client)
            .build()
            .create(FactsRestApi::class.java)
    }

    @Provides
    fun provideFactMapper() = FactMapper()

    @Provides
    fun provideFactRepository(api: FactsRestApi, mapper: FactMapper): FactRepository {
        return FactRepository(api = api, mapper = mapper)
    }
}