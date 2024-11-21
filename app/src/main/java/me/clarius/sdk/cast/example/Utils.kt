package me.clarius.sdk.cast.example

import java.util.Optional
import java.util.function.Function

object Utils {
    /**
     * Convert a string to an integer.
     *
     * @param from      input string.
     * @param transform a conversion function, for example Long::parseLong.
     * @param <T>       the integer type, for example Long.
     * @return the converted integer or an empty optional if the conversion failed.
    </T> */
    fun <T> maybeInteger(from: CharSequence?, transform: Function<String, T>): Optional<T> {
        try {
            val fromString = from.toString()
            val ret = transform.apply(fromString)
            return Optional.of(ret)
        } catch (e: NumberFormatException) {
            return Optional.empty()
        }
    }

    /**
     * Convert a string to a long.
     *
     * @param from input string.
     * @return the converted long or an empty optional if the conversion failed.
     */
    fun maybeLong(from: CharSequence?): Optional<Long> {
        return maybeInteger(
            from
        ) { s: String -> s.toLong() }
    }

    /**
     * Convert a string to an int.
     *
     * @param from input string.
     * @return the converted int or an empty optional if the conversion failed.
     */
    fun maybeInt(from: CharSequence?): Optional<Int> {
        return maybeInteger(
            from
        ) { s: String -> s.toInt() }
    }
}
