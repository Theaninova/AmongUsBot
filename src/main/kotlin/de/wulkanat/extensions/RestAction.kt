package de.wulkanat.extensions

import net.dv8tion.jda.api.requests.RestAction

fun <T> List<RestAction<T>>.queueAllSafe() {
    forEach {
        it.queueSafe()
    }
}

fun <T> RestAction<T>.queueSafe() {
    queue({}, {})
}

fun <T> RestAction<T>.queueSafe(success: (T) -> Unit) {
    queue(success) {}
}
