package com.rokoblak.chatbackup.data

data class Contact(val id: String, val name: String?, val number: String) {
    val displayName: String = name ?: number
}