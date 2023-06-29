package com.rokoblak.chatbackup.ui.notif.model


enum class NotificationType(
    val channelId: String,
    val channelName: String,
) {

    Main(
        "CHANNEL_MAIN",
        "Main"
    );
}