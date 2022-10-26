package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.domain.Element
import com.dogeweb.fuzzymatcher.domain.Token
import com.dogeweb.fuzzymatcher.exception.MatchException
import com.dogeweb.fuzzymatcher.util.Utils
import kotlin.jvm.JvmStatic

/**
 * A functional interface to Tokenize Elements
 */
object TokenizerFunction {
    private val soundex = Soundex()

    @JvmStatic
    val valueTokenizer: (Element<String>) -> List<Token<String>>
            = { listOf(Token(it.preProcessedValue, it)) }

    @JvmStatic
    val wordTokenizer: (Element<String>) -> List<Token<String>> = { element ->
        element.preProcessedValue
            .split("\\s+".toRegex())
            .dropLastWhile { it.isEmpty() }
            .map { Token(it, element) }
    }

    @JvmStatic
    val wordSoundexEncodeTokenizer: (Element<String>) -> List<Token<String>> = { element ->
        element.preProcessedValue.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
            .map {
                var code = it
                if (!Utils.isNumeric(it)) {
                    code = soundex.encode(it)
                    if (code == "") {
                        code = it
                    }
                }
                code
            }.map { Token(it, element) }
    }

    @JvmStatic
    val triGramTokenizer: (Element<String>) -> List<Token<String>> = { getNGramTokens(3, it) }

    @JvmStatic
    val decaGramTokenizer: (Element<String>) -> List<Token<String>> = { getNGramTokens(10, it) }

    fun getNGramTokens(size: Int, element: Element<out Any>): List<Token<String>> {
        val elementValue = element.preProcessedValue
        val elementValueStr: String = if (elementValue is String) {
            elementValue
        } else {
            throw MatchException("Unsupported data type")
        }
        return Utils.getNGrams(elementValueStr, size).map { Token(it, element) }
    }

    @JvmStatic
    fun <T : Any> chainTokenizers(vararg tokenizers: (Element<T>) -> List<Token<out Any>>): ((Element<T>) -> List<Token<out Any>>) {
        return { element ->
            tokenizers.flatMap{ it(element) }
        }
    }
}