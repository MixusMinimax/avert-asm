package util

fun <A, B> Pair<A, B>.bothOrNull() = let { (a, b) ->
    if (a != null && b != null) {
        a to b
    } else {
        null
    }
}
