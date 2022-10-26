package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.domain.*
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.triGramTokenizer
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.valueTokenizer
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.wordSoundexEncodeTokenizer
import com.dogeweb.fuzzymatcher.function.TokenizerFunction.wordTokenizer
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenizerFunctionTest {

    val document = document("dummy")

    @Test
    fun itShouldGetNGramTokenizer_Success() {
        val value = "james_parker"
        val elem = element(value = value, type = ElementType.EMAIL, document = document)
        assertEquals(if (value.length - 2 < 0) 1 else value.length - 2, triGramTokenizer(elem).count())
    }

    @Test
    fun itShouldReturnNGramTokenForSmallStrings_Success() {
        val value = "jp"
        val elem = element(value = value, type = ElementType.EMAIL, document = document)
        assertEquals(1, triGramTokenizer(elem).count())
    }

    @Test
    fun itShouldGetWordTokenizerForAddress_Success() {
        val value = "123 new Street, minneapolis mn"
        val elem = element(value = value, type = ElementType.ADDRESS, document = document)
        assertEquals(5, wordTokenizer(elem).count())
    }

    @Test
    fun itShouldGetWordTokenizerForName_Success() {
        val value = "James G. Parker"
        val elem = element(value = value, type = ElementType.NAME, document = document)
        assertEquals(3, wordTokenizer(elem).count())
    }

    @Test
    fun itShouldGetValueTokenizer_Success() {
        val value = "1234567890"
        val elem = element(value = value, type = ElementType.PHONE, document = document)
        assertEquals(1, valueTokenizer(elem).count())
        assertEquals("11234567890", valueTokenizer(elem).first().value)
    }

    @Test
    fun itShouldGetValueTokenizerForNumber_Success() {
        val value = "123.34"
        val elem = element(value = value, type = ElementType.NUMBER, document = document)
        assertEquals(1, valueTokenizer(elem).count())
        assertEquals("123.34", valueTokenizer(elem).first().value)
    }

    @Test
    fun itShouldTestExtraSpaceAndSpecialCharacters_Success() {
        val value = "12/3,    new     Street, minneapolis-   mn"
        val elem = element(value = value, type = ElementType.ADDRESS, document = document)
        assertEquals(5, wordTokenizer(elem).count())
    }

    @Test
    fun itShouldGetNGramTokenizerLongString() {
        val value =
            "thisStringIsUsedForTestingAReallyHumungusExtremlyLongAndLargeStringForEnsuringThePerformanceOfTheLuceneTokenizerFunction"
        val elem = element(value = value, type = ElementType.EMAIL, document = document)
        assertEquals(if (value.length - 2 < 0) 1 else value.length - 2, triGramTokenizer(elem).count())
        val value2 = "thisStringIsUsedForTestingAReallyHumungusExtremlyLongAndLargeString"
        val elem2 = element(value = value2, type = ElementType.EMAIL, document = document)
        assertEquals(if (value2.length - 2 < 0) 1 else value2.length - 2, triGramTokenizer(elem2).count())
    }

    @Test
    fun itShouldGetWordSoundexEncodeTokenizerForAddress() {
        val value = "123 new Street 23rd Ave"
        val elem = element(value = value, type = ElementType.ADDRESS, document = document)
        val results = wordSoundexEncodeTokenizer(elem)
        assertEquals(5, results.size)
        assertEquals("123", results[0].value)
        assertEquals("N000", results[1].value)
        assertEquals("S363", results[2].value)
        assertEquals("23rd", results[3].value)
    }

    @Test
    fun itShouldGetWordSoundexEncodeTokenizerForName() {
        val value1 = "Stephen Wilkson"
        val elem1 = element(value = value1, type = ElementType.NAME, document = document)
        val results1 = wordSoundexEncodeTokenizer(elem1)
        assertEquals("S315", results1[0].value)
        assertEquals("W425", results1[1].value)
        val value2 = "Steven Wilson"
        val elem2 = element(value = value2, type = ElementType.NAME, document = document)
        val results2 = wordSoundexEncodeTokenizer(elem2)
        assertEquals("S315", results2[0].value)
        assertEquals("W425", results2[1].value)
    }

    @Test
    fun itShouldCustomTokenizeText() {
        val value = "123a234a345"
        val defaultTokenElement = element(value = value, type = ElementType.TEXT, document = document)
        val defaultResults = defaultTokenElement.tokenizerFunction(defaultTokenElement)
        assertEquals(1, defaultResults.size)
        assertEquals(value, defaultResults[0].value)

        // Split the value with delimiter as character 'a'
        val customTokenFunc = { element: Element<String> ->
            element.preProcessedValue.split("a".toRegex()).dropLastWhile { it.isEmpty() }
                .map { Token(it, element) }
        }
        val customTokenElement = element(value = value, type = ElementType.TEXT, tokenizerFunction = customTokenFunc, document = document)
        val customResults = customTokenElement.tokenizerFunction(customTokenElement)
        assertEquals(3, customResults.size)
        assertEquals("123", customResults[0].value)
    }
}