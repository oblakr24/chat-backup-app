package com.rokoblak.chatbackup.services.notif


enum class NotificationType(
    val channelId: String,
    val channelName: String,
) {

    Main(
        "CHANNEL_MAIN",
        "Main"
    );
}