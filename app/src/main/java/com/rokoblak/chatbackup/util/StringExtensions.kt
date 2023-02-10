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

}