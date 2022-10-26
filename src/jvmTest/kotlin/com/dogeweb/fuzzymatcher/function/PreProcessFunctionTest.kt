package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.domain.*
import com.dogeweb.fuzzymatcher.function.PreProcessFunction.addressPreprocessing
import com.dogeweb.fuzzymatcher.function.PreProcessFunction.numberPreprocessing
import com.dogeweb.fuzzymatcher.function.PreProcessFunction.removeSpecialChars
import kotlin.test.Test
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.test.assertEquals

class PreProcessFunctionTest {

    val document = document("dummy")

    @Test
    fun itShouldRemoveSuffixFromName_Success() {
        val value = "James Parker JR."
        val element = element(value = value, type = ElementType.NAME, document = document)
        assertEquals("james parker", element.preProcessedValue)
    }

    @Test
    fun itShouldPreprocessAddress() {
        val value = "123 XYZ Ltd st, TX"
        val element = element(value = value, type = ElementType.ADDRESS, document = document)
        assertEquals("123 xyz ltd street texas", element.preProcessedValue)
    }

    @Test
    fun itShouldCustomPreprocessAddress() {
        val value = "123_XYZ_Ltd_st, TX"
        val customPreProcessing = { it: String -> it.replace("_".toRegex(), " ").let(addressPreprocessing) }
        val element = element(value = value, type = ElementType.ADDRESS, preProcessFunction = customPreProcessing, document = document)
        assertEquals("123 xyz ltd street texas", element.preProcessedValue)
    }

    @Test
    fun itShouldGetNullString_Success() {
        val value = "   "
        val element = element(value = value, type = ElementType.NAME, document = document)
        assertEquals("", element.preProcessedValue)
    }

    @Test
    fun itShouldRemoveTrailingNumbersFromName_Success() {
        val value = "Nova LLC-1"
        val element = element(value = value, type = ElementType.NAME, document = document)
        assertEquals("nova", element.preProcessedValue)
    }

    @Test
    fun itShouldTestComposingFunction() {
        val value = "James Parker jr."
        val element = element(value = value, type = ElementType.TEXT, document = document)
        assertEquals("james parker jr", element.preProcessedValue)
        val element1 = element(value = value, type = ElementType.TEXT,
            preProcessFunction = { it.replace("jr.", "").let(removeSpecialChars) }, document = document)
        assertEquals("james parker", element1.preProcessedValue)
    }

    @Test
    fun itShouldNormalizeAddress_Success() {
        val value = "123 some-street ave PLano, TX"
        val element = element(value = value, type = ElementType.ADDRESS, document = document)
        assertEquals("123 somestreet avenue plano texas", element.preProcessedValue)
    }

    @Test
    fun itShouldRemoveSpecialCharPhone_Success() {
        val value = "+1-(123)-456-4345"
        val result = removeSpecialChars(value) as String
        assertEquals("11234564345", result)
    }

    @Test
    fun itShouldApplyNumberPreprocessing_Success() {
        val value = "$ value -34.76"
        val result = numberPreprocessing(value) as String
        assertEquals("-34.76", result)
    }

    @Test
    fun itShouldApplyNumberPreprocessing_Failure() {
        val value = "$ value thirty four"
        val result = numberPreprocessing(value) as String
        assertEquals(value, result)
    }

    @Test
    @Throws(ParseException::class)
    fun itShouldApplyNonePreprocessing_Success() {
        val df: DateFormat = SimpleDateFormat("MM/dd/yyyy")
        val input = df.parse("04/01/2020")
        val result = PreProcessFunction.none(input) as Date
        assertEquals(input, result)
    }
}