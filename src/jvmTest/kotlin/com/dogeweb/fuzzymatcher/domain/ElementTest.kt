package com.dogeweb.fuzzymatcher.domain

import com.dogeweb.fuzzymatcher.component.match
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.chainTokenizers
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.triGramTokenizer
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.wordSoundexEncodeTokenizer
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.wordTokenizer
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTest {

    @Test
    fun itShouldSetTokenizerFunction() {
        val names = listOf("Brian Wilson", "Bryan Wilkson")

        // Test with Default using Soundex Tokenizer
        val documents1 = getDocuments(names)
        val result1 = documents1.match()
        assertEquals(2, result1.size)
        assertEquals(1.0, result1[documents1[0]]!![0].result, .01)

        // Test with override
        val documents2 = getDocuments(names, wordTokenizer)
        val result2 = documents2.match()
        assertEquals(0, result2.size)
    }

    @Test
    fun itShouldNotMatchPhoneticWordsWithChainTokenizerFunction() {
        val names = listOf("bold", "bolt")
        val documents1 = getDocuments(names, wordSoundexEncodeTokenizer)
        val result1 = documents1.match()
        assertEquals(2, result1.size)
        assertEquals(1.0, result1[documents1[0]]!![0].result, .01)
        val documents2 = getDocuments(
            names,
            chainTokenizers(
                wordTokenizer,
                wordSoundexEncodeTokenizer,
                triGramTokenizer
            )
        )
        val result2 = documents2.match()
        assertEquals(0, result2.size)
    }

    @Test
    fun itShouldNotMatchPhoneticWordsWithChainTokenizerFunction2() {
        val names = listOf("Caputo", "Chabot")
        val documents1 = getDocuments(names, wordSoundexEncodeTokenizer)
        val result1 = documents1.match()
        assertEquals(2, result1.size)
        assertEquals(1.0, result1[documents1[0]]!![0].result, .01)
        val documents2 = getDocuments(names, chainTokenizers(wordSoundexEncodeTokenizer, triGramTokenizer))
        val result2 = documents2.match()
        assertEquals(0, result2.size)
    }

    @Test
    fun itShouldMatchUnequalWordsWithChainTokenizerFunction() {
        val names = listOf("Mario", "Marieo")
        val documents1 = getDocuments(names, wordTokenizer)
        val result1 = documents1.match()
        assertEquals(0, result1.size)
        val documents2 = getDocuments(names, chainTokenizers(wordSoundexEncodeTokenizer, triGramTokenizer))
        val result2 = documents2.match()
        assertEquals(2, result2.size)
        assertEquals(0.6, result2[documents1[0]]!![0].result, .01)
    }

    @Test
    fun itShouldMatchUnequalWordsWithChainTokenizerFunction2() {
        val names = listOf("Nikolau", "Nikolaou")
        val documents1 = getDocuments(names, wordTokenizer)
        val result1 = documents1.match()
        assertEquals(0, result1.size)
        val documents2 = getDocuments(names, chainTokenizers(wordTokenizer, triGramTokenizer))
        val result2 = documents2.match()
        assertEquals(2, result2.size)
        assertEquals(0.58, result2[documents1[0]]!![0].result, .01)
    }

    private fun getDocuments(names: List<String>, tokenizerFunction: ((Element<String>) -> List<Token<out Any>>)? = null): List<Document> {
        var counter = 0
        return names.map {
           document("${++counter}") {
                name(it, tokenizerFunction = tokenizerFunction)
            }
        }
    }
}