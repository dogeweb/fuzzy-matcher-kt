package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.function.ScoringFunction
import com.dogeweb.fuzzymatcher.function.ScoringFunctions
import kotlin.math.abs
import kotlin.math.max

/**
 *
 *
 * The primary object for matching. The required attribute is a unique key and elements
 *
 *
 * Configurable attributes
 *
 *  * elements - A set of Element object to match against
 *  * threshold - Value above which documents are considered a match, default 0.5
 *
 */

class Document(
    val key: String,
    val threshold: Double,
    override val scoringFunction: ScoringFunction,
    receiver: PrivateDocumentBuilder.() -> Unit
) : Matchable {

    override val weight: Double = 1.0
    val elements: Set<Element<out Any>>

    init {
        val builder = PrivateDocumentBuilder(receiver)
        this.elements = builder.elements.toSet()
    }


    val typesCount by lazy { preProcessedElement.map { it.elementClassification }.groupingBy { it }.eachCount() }
    val preProcessedElement by lazy {
        elements
            .asSequence()
            .distinctBy { it.preprocessedValueWithType }
            .filter {
                (it.preProcessedValue as? String)?.isNotEmpty() ?: true
            }.toSet()
    }

    override fun getChildCount(other: Matchable): Long {
        if (other is Document) {
            return (typesCount.keys union other.typesCount.keys).sumOf {
                max((typesCount[it] ?: 0), (other.typesCount[it] ?: 0))
            }.toLong()
        }
        return 0
    }

    override fun getUnmatchedChildCount(other: Matchable): Long {
        if (other is Document) {
            return ((typesCount.keys union other.typesCount.keys) subtract (typesCount.keys intersect other.typesCount.keys)).sumOf {
                abs((typesCount[it] ?: 0) - (other.typesCount[it] ?: 0))
            }.toLong()
        }
        return 0
    }

    override fun toString(): String {
        return "{$orderedElements}"
    }

    private val orderedElements: List<Element<*>>
        get() = elements.sortedBy { it.elementClassification.elementType }

    override fun equals(other: Any?): Boolean {
        return this === other || ((other as? Document)?.key == key)
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    inner class PrivateDocumentBuilder(receiver: PrivateDocumentBuilder.() -> Unit) : DocumentBuilder() {

        val elements = mutableSetOf<Element<out Any>>()

        init {
            receiver(this)
        }

        override fun <T : Any> element(
            value: T,
            type: ElementType,
            variance: String?,
            weight: Number?,
            threshold: Number?,
            neighborhoodRange: Number?,
            preProcessFunction: ((T) -> T)?,
            matchType: MatchType?,
            tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)?
        ) {
            elements.add(
                Element<T>(
                    type,
                    variance,
                    value,
                    weight?.toDouble() ?: Element.ELEMENT_DEFAULT_WEIGHT,
                    threshold?.toDouble() ?: Element.ELEMENT_DEFAULT_THRESHOLD,
                    neighborhoodRange?.toDouble() ?: Element.ELEMENT_DEFAULT_NEIGHBORHOOD_RANGE,
                    preProcessFunction,
                    tokenizerFunction,
                    matchType,
                    this@Document
                )
            )
        }

    }
}