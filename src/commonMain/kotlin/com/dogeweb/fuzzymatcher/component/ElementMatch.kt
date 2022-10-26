package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.Element
import com.dogeweb.fuzzymatcher.domain.Match

class ElementMatch {

    private val tokenRepo: TokenRepo = TokenRepo()

    fun add(element: Element<out Any>) {
        element.tokens.forEach(tokenRepo::put)
    }

    fun match(element: Element<out Any>) =
        element.tokens
            .flatMap { token -> tokenRepo[token].map { token.element to it } }
            .groupingBy { it }
            .eachCount()
            .mapNotNull {
                with(it.key.first.getScore(it.value, it.key.second)) {
                    if (this > it.key.first.threshold) Match(it.key.first, it.key.second, this) else null
                }
            }.toSet()

    fun matchThenAdd(element: Element<out Any>): Set<Match<Element<out Any>>> {
        val matchElements = match(element)
        add(element)
        return matchElements
    }
}