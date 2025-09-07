package com.example.csschallenge.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApplicationModuleProvider {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi): Retrofit {
        return Retrofit.Builder()
//            .baseUrl("http://localhost:8080/")
            .baseUrl("http://10.0.2.2:8080/") // use 10.0.2.2 for emulator
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}