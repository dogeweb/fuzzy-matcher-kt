package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.function.ScoringFunction

/**
 *
 * Interface implemented by Document, Element and Token to enable matching and scoring these objects
 */
interface Matchable {
    fun getChildCount(other: Matchable): Long
    val scoringFunction: ScoringFunction
    val weight: Double
    fun getUnmatchedChildCount(other: Matchable): Long
}