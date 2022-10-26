package com.dogeweb.fuzzymatcher.domain

/**
 * Defines how each element is classified using ElementType and variance.
 *
 *  * ElementType is an enum which gives a template on all the functions that should be applied during match
 *  * Variance is a user defined String, that allows multiple ElementType to be defined in a Document
 *
 */
data class ElementClassification(val elementType: ElementType, val variance: String?) {

    override fun equals(other: Any?) = this === other || with(other as? ElementClassification) {
        elementType == this?.elementType && variance == this.variance
    }

    override fun hashCode() = arrayOf(elementType, variance).contentHashCode()
}