package com.rokoblak.chatbackup.util

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.activity.result.ActivityResultLauncher

object MessagingUtils {

    fun launchChangeDefaultPrompt(context: Context, launcher: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            val roleRequestIntent =
                roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            launcher.launch(roleRequestIntent)
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
            (context as? Activity)?.startActivity(intent)
        }
    }
}