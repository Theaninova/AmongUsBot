package de.wulkanat.extensions

fun <T> MutableList<T>.alsoAddIf(condition: Boolean, vararg args: T): MutableList<T> {
    if (condition) {
        addAll(args)
    }

    return this
}

fun <T> MutableList<T>.alsoAdd(vararg args: T): MutableList<T> {
    addAll(args)
    return this
}

fun <T> MutableList<T>.addAll(vararg elements: T) {
    addAll(elements)
}