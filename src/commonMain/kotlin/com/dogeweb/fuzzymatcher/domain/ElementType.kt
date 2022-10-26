package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.function.PreProcessFunction
import com.dogeweb.fuzzymatcher.function.TokenizerFunction

/**
 * Enum to define different types of Element.
 * This is used only to categorize the data, and apply functions at different stages of match.
 * The functions, can be overridden from Element class using the appropriate setters at the time of creation.
 */
enum class ElementType {
    NAME, TEXT, ADDRESS, EMAIL, PHONE, NUMBER, DATE, AGE;

    val preProcessFunction
        get() = when (this) {
                NAME -> PreProcessFunction.namePreprocessing
                TEXT -> PreProcessFunction.removeSpecialChars
                ADDRESS -> PreProcessFunction.addressPreprocessing
                EMAIL -> PreProcessFunction.removeDomain
                PHONE -> PreProcessFunction.usPhoneNormalization
                NUMBER, AGE -> PreProcessFunction.numberPreprocessing
                else -> PreProcessFunction.none
            }

    val tokenizerFunction
        get() = when (this) {
            NAME -> TokenizerFunction.wordSoundexEncodeTokenizer
            TEXT -> TokenizerFunction.wordTokenizer
            ADDRESS -> TokenizerFunction.wordSoundexEncodeTokenizer
            EMAIL -> TokenizerFunction.triGramTokenizer
            PHONE -> TokenizerFunction.decaGramTokenizer
            else -> TokenizerFunction.valueTokenizer
        }

    val matchType: MatchType
        get() = when (this) {
            NUMBER, DATE, AGE -> MatchType.NEAREST_NEIGHBORS
            else -> MatchType.EQUALITY
        }
}