package com.rokoblak.chatbackup.data

data class Contact(val id: String, val name: String?, val number: String) {
    val displayName: String = name ?: number
    private val displayNameOfLetters: String? = name ?: number.takeIf { it.firstOrNull()?.isLetter() == true }

    val initials: String? by lazy {
        val display = displayNameOfLetters ?: return@lazy null
        display.split(" ", limit = 2).joinToString(separator = "") {
            it.first().uppercase()
        }
    }
}