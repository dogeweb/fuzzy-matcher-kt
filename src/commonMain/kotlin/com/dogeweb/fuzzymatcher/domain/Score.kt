package com.dogeweb.fuzzymatcher.domain

/**
 * This holds the result of matching 2 Documents, Elements or Tokens.
 * This also holds the reference of the Match object, used to aggregate score with the ScoringFunction.
 */
class Score(val result: Double, val match: Match<*>) {

    override fun toString(): String {
        return "Score{" +
                "result=" + result +
                ", match=" + match +
                '}'
    }
}