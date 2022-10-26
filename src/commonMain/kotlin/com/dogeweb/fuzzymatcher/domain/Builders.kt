package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.function.ScoringFunction
import com.dogeweb.fuzzymatcher.function.ScoringFunctions

fun document(
    key: String,
    threshold: Double = 0.5,
    scoringFunction: ScoringFunction = ScoringFunctions.exponentialWeightedAverageScore,
    receiver: DocumentBuilder.() -> Unit = {}
): Document {
    return Document(key, threshold, scoringFunction, receiver)
}

fun <T : Any> element(
    value: T,
    type: ElementType,
    variance: String? = null,
    weight: Number? = null,
    threshold: Number? = null,
    neighborhoodRange: Number? = null,
    preProcessFunction: ((T) -> T)? = null,
    matchType: MatchType? = null,
    tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)? = null,
    document: Document
) = Element<T>(
    type,
    variance,
    value,
    weight?.toDouble() ?: Element.ELEMENT_DEFAULT_WEIGHT,
    threshold?.toDouble() ?: Element.ELEMENT_DEFAULT_THRESHOLD,
    neighborhoodRange?.toDouble() ?: Element.ELEMENT_DEFAULT_NEIGHBORHOOD_RANGE,
    preProcessFunction,
    tokenizerFunction,
    matchType,
    document
)

abstract class DocumentBuilder {

    operator fun <T : Any> Any.invoke(
        value: T,
        variance: String? = null,
        weight: Number? = null,
        threshold: Number? = null,
        neighborhoodRange: Number? = null,
        preProcessFunction: ((T) -> T)? = null,
        matchType: MatchType? = null,
        tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)? = null
    ) {
        val type = when {
            this === name -> ElementType.NAME
            this === text -> ElementType.TEXT
            this === address -> ElementType.ADDRESS
            this === email -> ElementType.EMAIL
            this === phone -> ElementType.PHONE
            this === number -> ElementType.NUMBER
            this === date -> ElementType.DATE
            this === age -> ElementType.AGE
            else -> return
        }
        element(
            value,
            type,
            variance,
            weight,
            threshold,
            neighborhoodRange,
            preProcessFunction,
            matchType,
            tokenizerFunction
        )
    }

    abstract fun <T : Any> element(
        value: T,
        type: ElementType,
        variance: String? = null,
        weight: Number? = null,
        threshold: Number? = null,
        neighborhoodRange: Number? = null,
        preProcessFunction: ((T) -> T)? = null,
        matchType: MatchType? = null,
        tokenizerFunction: ((Element<T>) -> List<Token<out Any>>)? = null
    )

    // NAME, TEXT, ADDRESS, EMAIL, PHONE, NUMBER, DATE, AGE;
    var name = "name"
        set(value) = element(value, ElementType.NAME)
    var text = "text"
        set(value) = element(value, ElementType.TEXT)
    var address = "address"
        set(value) = element(value, ElementType.ADDRESS)
    var email = "email"
        set(value) = element(value, ElementType.EMAIL)
    var phone = "phone"
        set(value) = element(value, ElementType.PHONE)
    var number = "number"
        set(value) = element(value, ElementType.NUMBER)
    var date = "date"
        set(value) = element(value, ElementType.DATE)
    var age = "age"
        set(value) = element(value, ElementType.AGE)

}