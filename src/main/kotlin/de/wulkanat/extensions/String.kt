package de.wulkanat.extensions

fun String.isProbablyHash(length: Int): Boolean {
    return trim() matches "([A-Z]|\\d){$length}".toRegex()
}