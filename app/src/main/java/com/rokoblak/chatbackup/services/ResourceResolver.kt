package com.rokoblak.chatbackup.services

import androidx.annotation.ArrayRes
import com.rokoblak.chatbackup.di.AppScope
import javax.inject.Inject

class ResourceResolver @Inject constructor(private val appScope: AppScope) {

    fun resolveStringArray(@ArrayRes id: Int): Array<String> =
        appScope.appContext.resources.getStringArray(id)
}