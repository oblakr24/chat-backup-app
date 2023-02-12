package com.rokoblak.chatbackup.di

import com.rokoblak.chatbackup.navigation.AppRouteNavigator
import com.rokoblak.chatbackup.navigation.RouteNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    @ViewModelScoped
    fun bindRouteNavigator(): RouteNavigator = AppRouteNavigator()
}