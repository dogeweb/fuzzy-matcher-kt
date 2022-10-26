package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.Element
import com.dogeweb.fuzzymatcher.domain.ElementType
import com.dogeweb.fuzzymatcher.domain.ElementType.ADDRESS
import com.dogeweb.fuzzymatcher.domain.ElementType.NAME
import com.dogeweb.fuzzymatcher.domain.document
import com.dogeweb.fuzzymatcher.function.TokenizerFunction
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementMatchTest {

    private val elementMatch = ElementMatch()
    var counter = 0

    @Test
    fun itShouldNotScoreMoreThanOneForName() {
        val element1 = getElement(NAME, "Rodrigue Rodrigues")
        val element2 = getElement(NAME, "Rodrigues, Rodrigue")
        val matchSet1 = elementMatch.matchThenAdd(element1)
        assertEquals(0, matchSet1.size)
        val matchSet2 = elementMatch.match(element2)
        assertEquals(1, matchSet2.size)
        assertEquals(1.0, matchSet2.iterator().next().result, 0.0)
    }

    @Test
    fun itShouldNotScoreMoreThanOneForAddress() {
        val element1 = getElement(ADDRESS, "325 NS 3rd Street Ste 567 Miami FL 33192")
        val element2 = getElement(ADDRESS, "325 NS 3rd Street Ste 567 Miami FL 33192")
        elementMatch.add(element1)
        val matchSet = elementMatch.match(element2)
        assertEquals(1, matchSet.size)
        assertEquals(1.0, matchSet.iterator().next().result, 0.0)
    }

    @Test
    fun itShouldGiveAverageScoreWithBalancedElements() {
        // 2 match strings out of 5 max - score 0.4
        val element1 = getElement(ADDRESS, "54th Street 546th avenue florida ")
        val element2 = getElement(ADDRESS, "95th Street 765th avenue Texas")
        elementMatch.add(element1)
        val matchSet = elementMatch.match(element2)
        assertEquals(1, matchSet.size)
        assertEquals(0.4, matchSet.iterator().next().result, 0.0)
    }

    @Test
    fun itShouldGiveAverageScoreWithUnbalancedElements() {
        // 3 out of 5 - Score 0.6
        val element1 = getElement(ADDRESS, "123 new st. ")
        val element2 = getElement(ADDRESS, "123 new street. Minneapolis MN")
        println(TokenizerFunction.wordSoundexEncodeTokenizer(element1))
        println(TokenizerFunction.wordSoundexEncodeTokenizer(element2))
        elementMatch.add(element1)
        val matchSet = elementMatch.match(element2)
        assertEquals(1, matchSet.size)
        assertEquals(0.6, matchSet.iterator().next().result, 0.0)
    }

    @Test
    fun itShouldNotMatch() {
        val element1 = getElement(ADDRESS, "456 college raod, Ohio")
        val element2 = getElement(ADDRESS, "123 new street. Minneapolis MN")
        elementMatch.add(element1)
        val matchSet = elementMatch.match(element2)
        assertEquals(0, matchSet.size)
    }

    @Test
    fun itShouldMatchElementsWithRepeatingTokens() {
        val element1 = getElement(ADDRESS, "123 new Street new street")
        val element2 = getElement(ADDRESS, "123 new Street")
        val matchSet1 = elementMatch.matchThenAdd(element1)
        assertEquals(0, matchSet1.size)
        val matchSet2 = elementMatch.match(element2)
        assertEquals(1, matchSet2.size)
        assertEquals(1.0, matchSet2.iterator().next().result, 0.0)
        assertEquals(matchSet2.iterator().next().data, element2)
        assertEquals(matchSet2.iterator().next().matchedWith, element1)
    }

    @Test
    fun itShouldMatchUnorderedTokens() {
        val element1 = getElement(ADDRESS, "James Parker")
        val element2 = getElement(ADDRESS, "Parker Jamies")
        elementMatch.add(element1)
        val matchSet = elementMatch.match(element2)
        assertEquals(1, matchSet.size)
        assertEquals(1.0, matchSet.iterator().next().result, 0.0)
    }

    private fun <T : Any> getElement(elementType: ElementType, value: T): Element<T> = document("${(++counter)}") {
        element(value, elementType)
    }.preProcessedElement.first() as Element<T>

}