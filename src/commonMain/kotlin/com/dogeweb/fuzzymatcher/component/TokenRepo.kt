package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.*
import com.dogeweb.fuzzymatcher.exception.MatchException
import kotlinx.datetime.*
import kotlin.math.abs


class TokenRepo {
    //    private val repoMap = ConcurrentHashMap<ElementClassification, Repo?>()
    private val repoMap: MutableMap<ElementClassification, Repo> = mutableMapOf()

    fun put(token: Token<out Any>) {
        val elementClassification = token.element.elementClassification
        var repo = repoMap[elementClassification]
        if (repo == null) {
            repo = Repo(token.element.matchType)
            repoMap[elementClassification] = repo
        }
        repo.put(token, token.element)
    }

    operator fun get(token: Token<out Any>): Set<Element<out Any>> {
        val repo = repoMap[token.element.elementClassification]
        return repo?.get(token) ?: emptySet()
    }

    private inner class Repo(var matchType: MatchType) {
        val tokenElementSet: MutableMap<Any, MutableSet<Element<out Any>>> by lazy { mutableMapOf() }
        val tokenBinaryTree by lazy { mutableListOf<Any>() }
        private val AGE_PCT_OF = 10.0
        private val DATE_PCT_OF = 15777e7 // 5 years of range

        fun put(token: Token<out Any>, element: Element<out Any>) {
            if (matchType == MatchType.NEAREST_NEIGHBORS) tokenBinaryTree.add(token.value)
            val elements = tokenElementSet[token.value] ?: hashSetOf()
            elements.add(element)
            tokenElementSet[token.value] = elements
        }

        operator fun <T : Any> get(token: Token<T>): Set<Element<T>> {
            return when (matchType) {
                MatchType.EQUALITY          -> tokenElementSet[token.value] as? Set<Element<T>> ?: emptySet()
                MatchType.NEAREST_NEIGHBORS -> {
                    val tokenRange = when (token.element.elementClassification.elementType) {
                        ElementType.AGE  -> TokenRange(token, token.element.neighborhoodRange, AGE_PCT_OF)
                        ElementType.DATE -> TokenRange(token, token.element.neighborhoodRange, DATE_PCT_OF)
                        else             -> TokenRange(token, token.element.neighborhoodRange)
                    }
//                    tokenBinaryTree.subSet(tokenRange.lower, true, tokenRange.higher, true)
//                        .mapNotNull { tokenElementSet[it] }.flatten().toSet()
                    tokenBinaryTree.asSequence().mapNotNull { it as? Comparable<T> }
                        .filter { (it >= tokenRange.lower) && (it <= tokenRange.higher) }
                        .mapNotNull { tokenElementSet[it] as? Set<Element<T>> }.flatten().toSet()
                }
            }
        }
    }

    class TokenRange<T : Any> (
        token: Token<T>, pct: Double, pctOf: Double? = null
    ) {
        var lower: T
        var higher: T

        init {
            when (val value = token.value) {
                is Double       -> {
                    lower = getLower(value, pct, pctOf).toDouble() as T
                    higher = getHigher(value, pct, pctOf).toDouble() as T
                }

                is Int           -> {
                    lower = getLower(value, pct, pctOf).toInt() as T
                    higher = getHigher(value, pct, pctOf).toInt() as T
                }

                is Long          -> {
                    lower = getLower(value, pct, pctOf).toLong() as T
                    higher = getHigher(value, pct, pctOf).toLong() as T
                }

                is Float         -> {
                    lower = getLower(value, pct, pctOf).toFloat() as T
                    higher = getHigher(value, pct, pctOf).toFloat() as T
                }

                is Instant       -> {
                    lower =
                        Instant.fromEpochMilliseconds(getLower(value.toEpochMilliseconds(), pct, pctOf).toLong()) as T
                    higher =
                        Instant.fromEpochMilliseconds(getHigher(value.toEpochMilliseconds(), pct, pctOf).toLong()) as T
                }

                is LocalDate     -> {
                    lower = Instant.fromEpochMilliseconds(
                        getLower(
                            value.atTime(0, 0).toInstant(UtcOffset.ZERO).toEpochMilliseconds(), pct, pctOf
                        ).toLong()
                    ).toLocalDateTime(
                        TimeZone.UTC
                    ).date as T
                    higher = Instant.fromEpochMilliseconds(
                        getHigher(
                            value.atTime(0, 0).toInstant(UtcOffset.ZERO).toEpochMilliseconds(), pct, pctOf
                        ).toLong()
                    ).toLocalDateTime(
                        TimeZone.UTC
                    ).date as T
                }

                is LocalDateTime -> {
                    lower = Instant.fromEpochMilliseconds(
                        getLower(
                            value.toInstant(UtcOffset.ZERO).toEpochMilliseconds(), pct, pctOf
                        ).toLong()
                    ).toLocalDateTime(
                        TimeZone.UTC
                    ) as T
                    higher = Instant.fromEpochMilliseconds(
                        getHigher(
                            value.toInstant(UtcOffset.ZERO).toEpochMilliseconds(), pct, pctOf
                        ).toLong()
                    ).toLocalDateTime(
                        TimeZone.UTC
                    ) as T
                }

                else             -> {
                    throw MatchException("Data Type not supported")
                }
            }
        }

        private fun getLower(number: Number, pct: Double, pctOf: Double?): Number {
            val dnum = number.toDouble()
            val dPctOf = pctOf ?: dnum
            val pctVal = abs(dPctOf * (1.0 - pct))
            return dnum - pctVal
        }

        private fun getHigher(number: Number, pct: Double, pctOf: Double?): Number {
            val dnum = number.toDouble()
            val dPctOf = pctOf ?: dnum
            val pctVal = abs(dPctOf * (1.0 - pct))
            return dnum + pctVal
        }
    }
}