package com.dogeweb.fuzzymatcher.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentTest {
    @Test
    fun itShouldGetUnmatchedCountForUnbalancedDoc() {

        val d1 =document("1") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = "parker@email.com"
        }
        val d2 =document("2") {
            name = "James Parker"
            address = ""
            phone = "123-123-1234"
            email = "parker@email.com"
        }
        assertEquals(4, d1.getChildCount(d2))
        assertEquals(4, d2.getChildCount(d1))
        assertEquals(2, d1.getUnmatchedChildCount(d2))
    }

    @Test
    fun itShouldGetUnmatchedCountForBalancedDoc() {
        val d1 =document("1") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = ""
        }
        val d2 =document("2") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = ""
        }
        assertEquals(2, d1.getChildCount(d2))
        assertEquals(0, d1.getUnmatchedChildCount(d2))
    }

    @Test
    fun itShouldGetUnmatchedCountForUnbalancedDocWithEmpty() {
        val d1 =document("1") {
            name = "James Parker"
            address = ""
            phone = "123"
            email = ""
        }
        val d2 =document("2") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = ""
        }
        assertEquals(3, d1.getChildCount(d2))
        assertEquals(2, d1.getUnmatchedChildCount(d2))
    }

    @Test
    fun itShouldGetUnmatchedCountForMultiElementTypes() {
        val d1 =document("1") {
            name = "James Parker"
            address = ""
            phone = "123"
            phone = "234"
            email = ""
        }
        val d2 =document("2") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = ""
        }
        assertEquals(4, d1.getChildCount(d2))
        assertEquals(3, d1.getUnmatchedChildCount(d2))
    }

    @Test
    fun itShouldGetUnmatchedCountForMultiElementTypesWithNonMatch() {
        val d1 =document("1") {
            name = "James Parker"
            address = ""
            phone = "123"
            phone = ""
            email = ""
        }
        val d2 =document("2") {
            name = "James Parker"
            address = "123 Some Street"
            phone = "567"
            email = ""
        }
        assertEquals(3, d1.getChildCount(d2))
        assertEquals(1, d1.getUnmatchedChildCount(d2))
    }
}