package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.component.TokenRepo
import com.dogeweb.fuzzymatcher.function.ScoringFunctions

/**
 *
 *
 * This class represent the string "value" against which match are run.
 *
 *
 * Configurable attributes
 *
 *  * type - The ElementType for the value. This determines the functions applied at different steps of the match
 *  * weight - Used in scoring function to increase the Document score for an Element. Default is 1.0 for all elements
 *  * threshold - Value above which elements are considered a match, default 0.3
 *  * neighborhoodRange - Relevant for NEAREST_NEIGHBORS MatchType. Defines how close should the value be, to be considered a match (default 0.9)
 *  * preProcessFunction - Function to pre-process the value. If this is not set, the function defined in ElementType is used
 *  * tokenizerFunction - Function to break values into tokens. If this is not set, the function defined in ElementType is used
 *  * matchType - MatchType used. If this is not set, the type defined in ElementType is used
 *
 */
class Element<T>(
    type: ElementType,
    variance: String? = null,
    val value: T,
    override val weight: Double,
    val threshold: Double,
    val neighborhoodRange: Double,
    preProcessFunction: ((T) -> T)? = null,
    tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)? = null,
    matchType: MatchType? = null,
    val document: Document
) : Matchable {

    companion object {
        const val ELEMENT_DEFAULT_WEIGHT = 1.0
        const val ELEMENT_DEFAULT_THRESHOLD = 0.3
        const val ELEMENT_DEFAULT_NEIGHBORHOOD_RANGE = 0.9
    }

    val elementClassification = ElementClassification(type, variance)
    val tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)
    private val preProcessFunction: (T) -> T
    val tokens: List<Token<out Any>>
    val matchType: MatchType
    val preProcessedValue: T
    val preprocessedValueWithType: Pair<ElementClassification, T>

    init {
        this.preProcessFunction = (preProcessFunction ?: type.preProcessFunction) as (T) -> T
        this.tokenizerFunction = (tokenizerFunction ?: type.tokenizerFunction) as (Element<T>) -> List<Token<out Any>>
        this.matchType = matchType ?: type.matchType
        this.preProcessedValue =
            (this.preProcessFunction(value) as? String)?.trim()?.lowercase() as T ?: this.preProcessFunction(value)
        preprocessedValueWithType = elementClassification to preProcessedValue
        tokens = tokenizerFunction(this).distinct()
    }

    fun getScore(matchingCount: Int, other: Element<*>): Double =
        matchingCount.toDouble() / getChildCount(other).toDouble()

    /**
     * This gets the Max number of tokens present between matching Elements.
     * For Elements that do not have a balanced set of tokens, it can push the score down.
     */
    override fun getChildCount(other: Matchable) = if (other is Element<*>) {
        listOf(this, other).maxOfOrNull { it.tokens.size }?.toLong() ?: 0
    } else 0

    override fun getUnmatchedChildCount(other: Matchable): Long {
        if (other is Element<*>) {
            return listOf(this, other).maxOfOrNull { it.tokens.count { it.value.toString().isEmpty() } }?.toLong() ?: 0
        }
        return 0
    }

    override val scoringFunction = ScoringFunctions.exponentialWeightedAverageScore

    override fun toString() = "{'$value'}"

    override fun equals(other: Any?) = this === other || if (other is Element<*>) {
        value == other.value && elementClassification == other.elementClassification
                && document == other.document
    } else false

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + elementClassification.hashCode()
//        result = 31 * result + document.hashCode()
        return result
    }
}