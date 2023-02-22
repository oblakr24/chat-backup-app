package com.rokoblak.chatbackup.util

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

object MessagingUtils {

    fun launchChangeDefaultPrompt(context: Context, launcher: ActivityResultLauncher<Intent>) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        val roleRequestIntent =
            roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
        launcher.launch(roleRequestIntent)
    }
}