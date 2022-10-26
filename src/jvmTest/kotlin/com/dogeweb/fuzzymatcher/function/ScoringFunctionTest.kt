package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.domain.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringFunctionTest {
    @Test
    fun itShouldGiveAverageScore_Success() {
        val document1 = getMockDocument(4L, 0L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 0.66),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.averageScore(match, childScores)
        assertEquals(0.41, score.result, 0.01)
    }

    @Test
    fun itShouldGiveAverageScoreWithEmptyFields_Success() {
        val document1 = getMockDocument(4L, 2L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 0.5),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.averageScore(match, childScores)
        assertEquals(.62, score.result, 0.01)
    }

    @Test
    fun itShouldGiveSimpleAverageScore_Success() {
        val document1 = getMockDocument(4L, 0L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 0.66),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.simpleAverageScore(match, childScores)
        assertEquals(0.41, score.result, 0.01)
    }

    @Test
    fun itShouldGetExponentialScoring_Success() {
        val document1 = getMockDocument(4L, 2L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 1.0),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.exponentialAverageScore(match, childScores)
        assertEquals(.79, score.result, 0.01)
    }

    @Test
    fun itShouldGiveWeightedAverageScore_Success() {
        val document1 = getMockDocument(4L, 2L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 1.0),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.weightedAverageScore(match, childScores)
        assertEquals(.8, score.result, 0.01)
    }

    @Test
    fun itShouldGetExponentialWeightedScoring_Success() {
        val document1 = getMockDocument(4L, 2L)
        val document2 = getMockDocument(4L, 0L)
        val match = getMockMatch(document1, document2)
        val childScores = getMockChildScores(
            Match(getMockElement(1.0, 1), mock(), 1.0),
            Match(getMockElement(2.0, 1), mock(), 1.0)
        )
        val score: Score = ScoringFunctions.exponentialWeightedAverageScore(match, childScores)
        assertEquals(.86, score.result, 0.01)
    }

    private fun getMockDocument(childCount: Long, emptyCount: Long) = mock<Document> {
        on { getChildCount(any()) } doReturn childCount
        on { getUnmatchedChildCount(any()) } doReturn emptyCount
    }

    private fun getMockElement(weight: Double, childCount: Long): Element<*> {
        val tokens = (0..childCount).map { mockToken }
        val element = mock<Element<out Any>> {
            on { this.weight } doReturn weight
            on { this.tokens } doReturn tokens
        }
        return element
    }

    private val mockToken: Token<out Any>
        private get() = mock()

    private fun getMockMatch(doc1: Document, doc2: Document): Match<Document> {
        return Match(doc1, doc2)
    }

    private fun getMockChildScores(vararg matches: Match<*>): List<Score> {
        return matches.map { it.score }
    }
}