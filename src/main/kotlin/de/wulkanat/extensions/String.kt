package de.wulkanat.extensions

fun String.isProbablyHash(length: Int): Boolean {
    if (!(trim() matches "([A-z]|\\d){$length}".toRegex())) return false

    var lastCase: Boolean? = null
    var consistentCasing = true
    var numbers = 0
    forEach {
        if (it.isDigit()) {
            numbers++
        } else {
            if (lastCase == null) {
                lastCase = it.isLowerCase()
            } else {
                if (lastCase != it.isLowerCase())
                    consistentCasing = false
            }
        }
    }

    return consistentCasing || numbers > 0
}