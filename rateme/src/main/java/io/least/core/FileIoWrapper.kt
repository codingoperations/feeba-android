package io.least.core

import java.io.File

fun readLocalFile(fileName: String): String {
    return File(fileName).readText()
}

suspend fun writeToLocalFile(text: String, fileName: String) {
    File(fileName).writeText(text)
}

