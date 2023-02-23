package com.rokoblak.chatbackup.data

import android.telephony.PhoneNumberUtils
import com.rokoblak.chatbackup.util.StringUtils
import java.util.*

data class Contact(
    val name: String?,
    val orgNumber: String,
    val avatarUri: String? = null,
    val phoneType: PhoneType = PhoneType.MOBILE,
) {
    // TODO: more reliable way of normalizing numbers?

    val number: String = normalizeNumber(orgNumber)

    val id: String = idFromNumber(number)

    val displayName: String = name ?: StringUtils.normalizePhoneNumber(number) ?: number

    private val displayNameOfLetters: String? =
        name?.takeIfStartsWithLetter() ?: number.takeIfStartsWithLetter()

    val initials: String? by lazy {
        val display = displayNameOfLetters ?: return@lazy null
        display.split(" ").filter { it.firstOrNull()?.isLetter() == true }.take(2)
            .joinToString(separator = "") {
                it.first().uppercase()
            }
    }

    private fun String.takeIfStartsWithLetter() = takeIf { it.firstOrNull()?.isLetter() == true }

    companion object {
        fun idFromNumber(number: String) = "C_${StringUtils.normalizePhoneNumber(number) ?: number}"
        fun normalizeNumber(orgNumber: String): String = orgNumber
            .let { num ->
                val noNumbers = num.none { it.digitToIntOrNull() != null }
                if (noNumbers.not()) {
                    num.let { PhoneNumberUtils.stripSeparators(it) }
                        .let { PhoneNumberUtils.normalizeNumber(it) }
                } else {
                    num
                }
            }
    }
}

enum class PhoneType {
    MOBILE, HOME, WORK, OTHER, PAGER, ASSISTANT, CAR, FAX_HOME, FAX_WORK, MAIN;

    fun displayName() = this.name.split("_").joinToString(separator = " ") { word ->
        word.lowercase()
            .replaceFirstChar { it.titlecase(Locale.US) }
    }
}