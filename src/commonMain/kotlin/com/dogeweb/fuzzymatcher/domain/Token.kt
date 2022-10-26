package com.dogeweb.fuzzymatcher.domain

/**
 * Elements are broken down into Token class using the TokenizerFunction
 */
class Token<T : Any>(val value: T, val element: Element<out Any>) {

    override fun toString(): String {
        return "{" +
                value +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false
        val token = o as Token<*>
        return value == token.value && element == token.element
    }

    override fun hashCode(): Int {
        return arrayOf(value, element).contentHashCode()
    }
}