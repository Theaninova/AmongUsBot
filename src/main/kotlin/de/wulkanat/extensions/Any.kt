package de.wulkanat.extensions

inline fun <T> T.alsoIf(condition: Boolean, then: T.() -> Unit): T {
    if (condition) {
        then()
    }
    return this
}