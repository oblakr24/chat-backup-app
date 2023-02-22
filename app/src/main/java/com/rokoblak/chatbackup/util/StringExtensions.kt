package com.rokoblak.chatbackup.util


object StringUtils {

    fun coerceFilename(filename: String): String {
        return if (filename.length > 22) {
            val ext = filename.substringAfterLast(".")
            val name = filename.substringBeforeLast(".")
            name.take(9) + "..." + name.takeLast(9) + "." + ext
        } else {
            filename
        }
    }

    fun normalizePhoneNumber(org: String): String? {
        val regex = Regex("\\D")
        val numbers = regex.replace(org, "")
        if (numbers.isEmpty()) return null

        val hasLeadingOne = numbers.first() == '1' && numbers.length != 7
        if (numbers.length != 7 && !(numbers.length == 10 && !hasLeadingOne) && !(numbers.length == 11 && hasLeadingOne)) {
            return null
        }

        val leadingOne = if (hasLeadingOne) "1 " else ""
        var currIdx = if (hasLeadingOne) 1 else 0

        var areaCode = ""
        if (numbers.length >= 10) {
            val areaCodeLength = 3
            val sub = numbers.substring(currIdx, currIdx + areaCodeLength)
            areaCode = "($sub) "
            currIdx += areaCodeLength
        }

        val prefixLength = 3
        val prefix = numbers.substring(currIdx, currIdx + prefixLength)
        currIdx += prefixLength

        val suffixLength = 4
        val suffix = numbers.substring(currIdx, currIdx + suffixLength)

        return "$leadingOne$areaCode$prefix-$suffix"
    }
}