package com.example.csschallenge.di

import com.example.csschallenge.data.repositories.OrderRepoImpl
import com.example.csschallenge.domain.repositories.OrderRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Binds
    abstract fun bindOrderRepo(orderRepoImpl: OrderRepoImpl): OrderRepo
}