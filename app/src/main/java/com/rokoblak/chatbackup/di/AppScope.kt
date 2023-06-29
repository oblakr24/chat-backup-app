package com.rokoblak.chatbackup.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppScope @Inject constructor(@ApplicationContext context: Context) {

    val appContext: Context = context
}