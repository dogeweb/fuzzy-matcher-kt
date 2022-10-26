package com.dogeweb.fuzzymatcher.domain

/**
 *
 *
 * A generic class to hold the match between 2 objects and the score of the match result.
 * A match between similar Token, Element or Document is represented by this class.
 *
 *
 * The "data" and "matchedWith" object holds the 2 records that matched. And "score" represents the match for these 2 objects.
 * "childScore" is used by ScoringFunction to aggregate and calculate the "score" value
 */
data class Match<T : Matchable> constructor(val data: T, val matchedWith: T) {

    val weight get() = data.weight
    lateinit var score: Score
        private set
    val result get() = score.result

    constructor(t: T, matchedWith: T, childScores: List<Score>) : this(t, matchedWith) {
        val maxDistinctChildScores = getMaxDistinctScores(childScores)
        score = data.scoringFunction(this, maxDistinctChildScores)
    }

    constructor(t: T, matchedWith: T, result: Double) : this(t, matchedWith) {
        score = Score(result, this)
    }

    private fun getMaxDistinctScores(scoreList: List<Score>) =
        scoreList.groupBy { it.match.data }.mapValues { it.value.maxBy { it.result } }.map { it.value }

    override fun toString(): String {
        return "Match{" +
                "data=" + data +
                ", matchedWith=" + matchedWith +
                ", score=" + score.result +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false
        val match = o as Match<*>
        return data == match.data && matchedWith == match.matchedWith
    }

    override fun hashCode() = arrayOf<Any>(data, matchedWith).contentHashCode()
}