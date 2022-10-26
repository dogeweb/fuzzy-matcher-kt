package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.domain.Match
import com.dogeweb.fuzzymatcher.domain.Score
import kotlin.math.pow

/**
 * A functional interface to get a score between 2 Match objects
 */

typealias ScoringFunction = (Match<*>, List<Score>) -> Score

object ScoringFunctions {

    /**
     * For all the childScores in a Match object it calculates the average.
     * To get a balanced average for 2 Match object which do not have same number of childScores.
     * It gives a score of 0.5 for missing children
     *
     * @return the scoring function for Average
     */
    val averageScore: ScoringFunction = { match, childScores ->
        val numerator = childScores.sumOfResult + match.unmatchedChildScore
        val denominator = match.childCount
        Score(numerator / denominator, match)
    }

    /**
     * For all the childScores in a Match object it calculates the average.
     * Average is calculated with a total of child scored divided by the child count
     *
     * @return the scoring function for Simple Average
     */
    val simpleAverageScore: ScoringFunction = { match, childScores ->
        val numerator = childScores.sumOfResult
        val denominator = match.childCount
        Score(numerator / denominator, match)
    }

    /**
     * Follows the same rules as "getAverageScore" and in addition applies weights to children.
     * It can be used for aggregating Elements to Documents, where weights can be provided at Element level
     *
     * @return the scoring function for WeightedAverage
     */
    val weightedAverageScore: ScoringFunction =
        { match, childScores ->
            val numerator = (childScores.sumOfWeightedResult
                    + match.unmatchedChildScore)
            val denominator = (childScores.sumOfWeights
                    + match.childCount
                    - childScores.size)
            Score(numerator / denominator, match)
        }

    /**
     * Follows the same rules as "getAverageScore", and in addition if more than 1 children match above a score of 0.9,
     * it exponentially increases the overall score by using a 1.5 exponent
     *
     * @return the scoring function for ExponentialAverage
     */
    val exponentialAverageScore: ScoringFunction =
        { match, childScores ->
            val perfectMatchedElements = childScores.perfectMatchedElement
            if (perfectMatchedElements.size > 1 && perfectMatchedElements.sumOfResult > 1) {
                val numerator = (perfectMatchedElements.sumOfResult.exponentiallyIncreasedValue
                        + childScores.nonPerfectMatchedElement.sumOfResult
                        + match.unmatchedChildScore)
                val denominator = (perfectMatchedElements.size.toDouble().exponentiallyIncreasedValue
                        + match.childCount
                        - perfectMatchedElements.size)
                Score(numerator / denominator, match)
            } else averageScore(match, childScores)
        }// Apply Exponent if match elements > 1

    /**
     * This is the default scoring used to calculate the Document score by aggregating the child Element scores.
     * This combines the benefits of weights and exponential increase when calculating the average scores.
     *
     * @return the scoring function for ExponentialWeightedAverage
     */
    val exponentialWeightedAverageScore: ScoringFunction =
        { match, childScores ->
            val perfectMatchedElements = childScores.perfectMatchedElement

            // Apply Exponent if match elements > 1
            if (perfectMatchedElements.size > 1 && perfectMatchedElements.sumOfWeightedResult > 1) {
                val notPerfectMatchedElements = childScores.nonPerfectMatchedElement
                val numerator = (perfectMatchedElements.sumOfWeightedResult.exponentiallyIncreasedValue
                        + notPerfectMatchedElements.sumOfWeightedResult
                        + match.unmatchedChildScore)
                val denominator = ((perfectMatchedElements.sumOfWeights.exponentiallyIncreasedValue
                        + notPerfectMatchedElements.sumOfWeights
                        + match.childCount)
                        - childScores.size)
                Score(numerator / denominator, match)
            } else weightedAverageScore(match, childScores)
        }

    private inline val List<Score>.sumOfWeightedResult: Double
        get() = sumOf { it.result * it.match.weight }

    private inline val List<Score>.sumOfResult: Double
        get() = sumOf { it.result }

    private inline val List<Score>.sumOfWeights: Double
        get() = sumOf { it.match.weight }

    private inline val Double.exponentiallyIncreasedValue: Double
        get() = pow(EXPONENT)

    private inline val List<Score>.nonPerfectMatchedElement: List<Score>
        get() = filter { it.result < EXPONENTIAL_INCREASE_THRESHOLD }

    private inline val List<Score>.perfectMatchedElement: List<Score>
        get() = filter { it.result >= EXPONENTIAL_INCREASE_THRESHOLD }

    private inline val Match<*>.childCount: Double
        get() = data.getChildCount(matchedWith).toDouble()

    private inline val Match<*>.unmatchedChildScore: Double
        get() = DEFAULT_UNMATCHED_CHILD_SCORE * data.getUnmatchedChildCount(matchedWith)

    private const val EXPONENT = 1.5
    private const val EXPONENTIAL_INCREASE_THRESHOLD = 0.9
    private const val DEFAULT_UNMATCHED_CHILD_SCORE = 0.5

}