package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.*
import com.dogeweb.fuzzymatcher.exception.MatchException
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenRepoTest {

    private var ai = 0
    private val document = document("dummy")

    @Test
    fun shouldGetForNameWithEquality() {
        val names = listOf("Amy Doe", "Brian Doe", "Jane Amy", "Michael Wane")
        val elements = getElements(names, ElementType.NAME, null)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 = element(value = "Amy", type = ElementType.NAME, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[0] in matchingElements1!!)
        assertTrue(elements[2] in matchingElements1)
        val testElement2 = element(value = "Doe", type = ElementType.NAME, document = document)
        val token2 = testElement2.tokens[0]
        val matchingElements2 = tokenRepo[token2]
        assertTrue(elements[0] in matchingElements2!!)
        assertTrue(elements[1] in matchingElements2)
    }

    @Test
    fun shouldGetForNumberWithNearestNeighbor() {
        val numbers = listOf(100, 200, 1, 25, 700, 99, 210, 500)
        val elements = getElements(numbers, ElementType.NUMBER)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 = element(value = 101, type = ElementType.NUMBER, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[0] in matchingElements1!!)
        assertTrue(elements[5] in matchingElements1)
        val testElement2 = element(value = 205, type = ElementType.NUMBER, document = document)
        val token2 = testElement2.tokens[0]
        val matchingElements2 = tokenRepo[token2]
        assertTrue(elements[1] in matchingElements2!!)
        assertTrue(elements[6] in matchingElements2)
    }

    @Test
    fun shouldGetForNumberForNegativeWithNearestNeighbor() {
        val numbers = listOf(-100, -200, -1, -25, -700, -99, -210, -500)
        val elements = getElements(numbers, ElementType.NUMBER)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 = element(value = -101, type = ElementType.NUMBER, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[0] in matchingElements1!!)
        assertTrue(elements[5] in matchingElements1)
        val testElement2 = element(value = -205, type = ElementType.NUMBER, document = document)
        val token2 = testElement2.tokens[0]
        val matchingElements2 = tokenRepo[token2]
        assertTrue(elements[1] in matchingElements2!!)
        assertTrue(elements[6] in matchingElements2)
    }

    @Test
    fun shouldGetForNumberWithEquality() {
        val numbers = listOf(100, 200, 1, 25, 700, 99, 210, 500)
        val elements = getElements(numbers, ElementType.NUMBER, MatchType.EQUALITY)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 = element(value = 100, type = ElementType.NUMBER, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[0] in matchingElements1!!)
        val testElement2 = element(value = 200, type = ElementType.NUMBER, document = document)
        val token2 = testElement2.tokens[0]
        val matchingElements2 = tokenRepo[token2]
        assertTrue(elements[1] in matchingElements2!!)
    }

    @Test
    fun shouldGetForNotSupportedWithNearestNeighbor() {
        assertFailsWith<MatchException> {
            val numbers = listOf("100", "200", "1", "25", "700", "99", "210", "500")
            val elements = getElements(numbers, ElementType.TEXT, MatchType.NEAREST_NEIGHBORS)
            val tokenRepo = TokenRepo()
            elements.forEach { it.tokens.forEach(tokenRepo::put) }
            val testElement1 = element(value = "101", type = ElementType.TEXT, document = document)
            val token1 = testElement1.tokens[0]
            tokenRepo[token1]
        }
    }

    @Test
    fun shouldGetForDateWithNearestNeighbor() {
        val numbers = listOf(
            getDate("2020-01-01T00:00:00Z"),
            getDate("2020-12-01T00:00:00Z"),
            getDate("2020-02-01T00:00:00Z"),
            getDate("1970-01-01T00:00:00Z"),
        )
        val elements = getElements(numbers, ElementType.DATE, null)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 =
            element(value = getDate("1970-01-01T02:00:00Z"), type = ElementType.DATE, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[3] in matchingElements1!!)
    }

    private fun getDate(value: String): Instant {
        return Instant.parse(value)
    }

    @Test
    fun shouldGetMultipleMatchedWithNearestNeighbour() {
        val numbers = listOf(100, 100)
        val elements = getElements(numbers, ElementType.NUMBER, null)
        val tokenRepo = TokenRepo()
        elements.forEach { it.tokens.forEach(tokenRepo::put) }
        val testElement1 = element(value = 100, type = ElementType.NUMBER, document = document)
        val token1 = testElement1.tokens[0]
        val matchingElements1 = tokenRepo[token1]
        assertTrue(elements[0] in matchingElements1!!)
        assertTrue(elements[1] in matchingElements1)
    }

    private fun getElements(values: List<Any>, elementType: ElementType, matchType: MatchType? = null): List<Element<*>> {
        return values.map { getelement(it, elementType, matchType) }
    }

    private fun getelement(value: Any, elementType: ElementType, matchType: MatchType?): Element<*> {
        return document((++ai).toString() + "") {
            element(value = value, type = elementType, matchType = matchType)
        }.elements.first()
    }
}