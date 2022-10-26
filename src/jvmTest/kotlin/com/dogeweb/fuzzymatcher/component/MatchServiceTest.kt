package com.dogeweb.fuzzymatcher.component

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.dogeweb.fuzzymatcher.domain.Document
import com.dogeweb.fuzzymatcher.domain.Element
import com.dogeweb.fuzzymatcher.domain.ElementType
import com.dogeweb.fuzzymatcher.domain.document
import com.dogeweb.fuzzymatcher.function.PreProcessFunction
import com.dogeweb.fuzzymatcher.function.ScoringFunctions
import getTestResource
import kotlinx.datetime.toLocalDate
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchServiceTest {

    @Test
    @Throws(IOException::class)
    fun itShouldApplyMatchForDemo() {
        val result = demoDocuments.matchById()
        result.forEach { entry ->
            entry.value.forEach { println("Data: ${it.data} Matched With: ${it.matchedWith} Score: ${it.score.result}") }
        }
        assertEquals(5, result.size)
    }

    @Test
    fun itShouldApplyMatchByDocId() {
        val input = arrayOf(
            arrayOf("1", "Steven Wilson", "45th Avenue 5th st."),
            arrayOf("2", "John Doe", "546 freeman ave"),
            arrayOf("3", "Stephen Wilkson", "45th Ave 5th Street")
        )
        val documentList = input.map {
            document(key = it[0]) {
                name = it[1]
                address = it[2]
            }
        }
        val result = documentList.matchById()
        result.asSequence().flatMap { it.value }
            .forEach { println("Data: ${it.data} Matched With: ${it.matchedWith} Score: ${it.score.result}") }
        assertEquals(2, result.size)
    }

    @Test
    fun itShouldApplyMatchByGroups() {
        val input = arrayOf(
            arrayOf("1", "Steven Wilson", "45th Avenue 5th st."),
            arrayOf("2", "John Doe", "546 freeman ave"),
            arrayOf("3", "Stephen Wilkson", "45th Ave 5th Street")
        )
        val documentList = input.map {
            document(it[0]) {
                name = it[1]
                address = it[2]
            }
        }
        val result = documentList.matchWithByGroups()
        result.asSequence().flatten()
            .forEach { println("Data: ${it.data} Matched With: ${it.matchedWith} Score: ${it.score.result}") }
        assertEquals(1, result.size)
    }

    @Test
    @Throws(IOException::class)
    fun itShouldApplyMatchByDocIdForSingleDoc() {
        val doc = document("TestMatch") {
            name = "john doe"
            address = "546 freeman ave dallas tx 75024"
            phone = "2122232235"
            email = "john@doe.com"
        }
        val result = doc matchWith testDocuments
//        writeOutput(result)
        assertEquals(1, result.size)
    }

    @Test
    @Throws(IOException::class)
    fun itShouldApplyMatchByDocIdForAList() {
        val result = testDocuments.matchById()
//        writeOutput(result)
        assertEquals(6, result.size)
    }

    @Test
    @Throws(IOException::class)
    fun itShouldApplyMatchByGroupsForAList() {
        val result = testDocuments.matchWithByGroups()
//        writeOutput(result)
        assertEquals(2, result.size)
    }

    @Test
    fun itShouldApplyMatchForMultiplePhoneNumber() {
        val inputData: MutableList<Document> = ArrayList()
        inputData.add(document("1") {
            name = "Kapa Limited"
            address = "texas"
            phone = "8204354957 xyz"
            phone = ""
            phone = "(848) 398-3868"
            email = "kirit@kapalimited.com"
        })
        inputData.add(document("2") {
            name = "Tram Kapa Ltd LLC"
            address = "texas"
            phone = "(848) 398-3868"
            phone = "(820) 435-4957"
            phone = ""
            email = "kirit@nekoproductions.com"
        })
        val result = inputData.match()
        assertEquals(2, result.size)
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
    }

    @Test
    fun itShouldApplyMatchForMultipleEmptyPhoneNumber() {
        val inputData = listOf(document("1") {
            name = "Kapa Limited"
            address = "texas"
            phone("8204354957 xyz", weight = 2.0, threshold = 0.5)
            phone("", weight = 2.0, threshold = 0.5)
            phone("(848) 398-3868", weight = 2.0, threshold = 0.5)
            email("kirit@kapalimited.com", weight = 2.0, threshold = 0.5)
        }, document("2") {
            name = "Tram Kapa Ltd LLC"
            address = "texas"
            phone("(848) 398-3868", weight = 2.0, threshold = 0.5)
            phone("(820) 435-4957", weight = 2.0, threshold = 0.5)
            phone("", weight = 2.0, threshold = 0.5)
            email = "kirit@nekoproductions.com"
        })
        val result = inputData.match()
        assertEquals(2, result.size)
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
    }

    @Test
    fun itShouldApplyMatchForDuplicateTokensWithNoMatch() {
        val inputData = listOf(document("1") {
            name = "lucky DAVID ABC"
            address = "123 W Plano St PLANO TX 33130"
            phone("", threshold = 0.5, weight = 2.0)
            phone("", threshold = 0.5, weight = 2.0)
            phone("", threshold = 0.5, weight = 2.0)
            email("", threshold = 0.5, weight = 2.0)
        }, document("2") {
            name = "Ramirez Yara"
            address = "123 W Plano St 2111 Plano TX 33130"
            phone("1231231234", weight = 2, threshold = 0.5)
            phone("", weight = 2, threshold = 0.5)
            phone("", weight = 2, threshold = 0.5)
            email("yara1345@gmail.com", threshold = 0.5)
        })
        val result = inputData.match()
        assertTrue(result.isEmpty())
    }


    @Test
    fun itShouldApplyMatch() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("(123) 234 2345", weight = 2, threshold = 0.5)
            email("jparker@gmail.com", threshold = 0.5)
        }, document("2") {
            name = "James"
            address = "123 new Street, minneapolis mn"
            phone("123-234-2345", weight = 2, threshold = 0.5)
            email("james_parker@domain.com", threshold = 0.5)
        })
        val result = inputData.match()
        assertEquals(2, result.size)
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
    }


    @Test
    fun itShouldApplyMatchWith3Documents() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("(123) 234 2345", weight = 2, threshold = 0.5)
            email("jparker@gmail.com", threshold = 0.5)
        }, document("2") {
            name = "James"
            address = "123 new Street, minneapolis mn"
            phone("123-234-2345", weight = 2, threshold = 0.5)
            email("james_parker@domain.com", threshold = 0.5)
        }, document("3") {
            name = "John D"
            address = "33 hammons Dr. Texas"
            phone("9901238484", weight = 2, threshold = 0.5)
            email("d_john@domain.com", threshold = 0.5)
        })
        val result = inputData.match()
        val totalMatches = result.asSequence().sumOf { it.value.size }
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
        assertEquals(2, totalMatches.toLong())
    }

    @Test
    fun itShouldApplyMatchWithFailure() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("(123) 234 2345", weight = 2, threshold = 0.5)
            email("jparker@gmail.com", threshold = 0.5)
        }, document("2") {
            name = "Peter Watson"
            address = "321 john Q Hammons Street, Plano, TX - 75054"
            phone("9091238877", weight = 2, threshold = 0.5)
            email("peter.watson@domain.com", threshold = 0.5)
        })
        val result = inputData.match()
        assertTrue(result.isEmpty())
    }


    @Test
    fun itShouldApplyMatchForMultipleEmptyField() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("", weight = 2, threshold = 0.5)
            email("", threshold = 0.5)
        }, document("2") {
            name = "James"
            address = "123 new street Minneapolis MN"
            phone("", weight = 2, threshold = 0.5)
            email("", threshold = 0.5)
        })
        val result = inputData.match()
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
        assertEquals(2, result.size)
    }

    @Test
    fun itShouldApplyMatchForEmptyInput() {
        val inputData = emptyList<Document>()
        val result = inputData.match()
        assertTrue(result.isEmpty())
    }

    @Test
    fun itShouldApplyMatchForWhiteSpaceWithNoFalsePositive() {
        val inputData = listOf(document("1") {
            name = "sdwet ert rdfgh, LLC"
            address = " "
            phone("", weight = 2, threshold = 0.5)
            email("sdwet@abc.com", threshold = 0.5)
        }, document("2") {
            name = "sad sdf LLC"
            address = " "
            phone("", weight = 2, threshold = 0.5)
            email("sad@something.com", threshold = 0.5)
        })
        val result = inputData.match()
        assertTrue(result.isEmpty())
    }

    //It tests whether there is any match between two different type element
    @Test
    fun itShouldApplyMatchElementsWithDifferentType() {
        val documents = listOf(document("1") {
            name = "John d"
            address = "freeman ave dallas 75024"
            phone("435-221-5432", weight = 2, threshold = 0.5)
            email = "john_doe@gmail.com"
        }, document("2") {
            name = "john doe"
            address = "546 freeman avenue dallas tx 75024"
            phone("435-334-2234", weight = 2, threshold = 0.5)
            email = "john@doe.com"
        })
        val result = documents.match()
        assertTrue(result.isEmpty())
    }


    @Test
    @Throws(FileNotFoundException::class)
    fun itShouldApplyMatchWithSourceList() {
        val sourceData = listOf(document("S1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("(123) 234 2345", weight = 2, threshold = 0.5)
            email("jparker@gmail.com", threshold = 0.5)
        }, document("S2") {
            name = "James"
            address = "123 new Street, minneapolis mn"
            phone("123-234-2345", weight = 2, threshold = 0.5)
            email("james_parker@domain.com", threshold = 0.5)
        })
        val result = sourceData matchWith testDocuments
        assert(result.map { it.key.key }.containsAll(listOf("S1", "S2")))
        assertEquals(2, result.size)
    }


    @Test
    @Throws(FileNotFoundException::class)
    fun itShouldApplyMatchWithSourceDocument() {
        val doc = listOf(document("S1") {
            name = "James Parker"
            address = "123 new st. Minneapolis MN"
            phone("(123) 234 2345", weight = 2, threshold = 0.5)
            email("jparker@gmail.com", threshold = 0.5)
        })
        val result = doc matchWith testDocuments
        assertContains(result.map { it.key.key }, "S1")
        assertEquals(1, result.size)
    }


    @Test
    fun itShouldApplyMatchWithScoreNotMoreThanOne() {
        val inputData = listOf(document("1") {
            name = "Kapa Limited"
            address = "123 some street, plano, texas - 75070"
            phone("123-456-7890", weight = 2, threshold = 0.5)
            phone("1234567890", weight = 2, threshold = 0.5)
            phone("", weight = 2, threshold = 0.5)
            email("kirit@kapalimited.com", threshold = 0.5)
        }, document("2") {
            name = "ABC CORP"
            address = "123 some street, plano, texas - 75070"
            phone("123-456-7890", weight = 2, threshold = 0.5)
            phone("1234567890", weight = 2, threshold = 0.5)
            phone("", weight = 2, threshold = 0.5)
            email("kirit@nekoproductions.com", threshold = 0.5)
        })
        val result = inputData.match()
        assertEquals(2, result.size)
        assert(
            result.map { it.key.key }.containsAll(listOf("1", "2"))
        )
        assertTrue(result[inputData.first()]!![0].result <= 1)
    }


    @Test
    @Throws(IOException::class)
    fun itShouldApplyMatchWithConfigurablePreProcessingFunctions() {
        val result1 = getTestData(
            PreProcessFunction.removeSpecialChars, PreProcessFunction.removeSpecialChars, 0.7
        ).match()
        result1.forEach(::println)
        println()
        assertEquals(2, result1.size)
        val result2 = getTestData(
            PreProcessFunction.namePreprocessing, PreProcessFunction.addressPreprocessing, 0.7
        ).match()
        result2.forEach(::println)
        assertEquals(4, result2.size)
    }


    @Test
    fun itShouldOverridePreProcessingDictionary() {
        val newNameDict = mapOf(
            "Queen" to "", "Third" to "", "III" to ""
        )
        val newNamePreProcessing = { str: String ->
            str.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.joinToString(" ") { newNameDict[it] ?: it }
        }
        val input = arrayOf(arrayOf("1", "Victoria Third"), arrayOf("2", "Queen Victoria III"))
        val documentList = input.map {
            document(it[0]) {
                name(it[1], preProcessFunction = newNamePreProcessing)// Set the custom function
            }
        }
        val result = documentList.match()
        assertEquals(2, result.size)
    }

    @Test
    @Throws(FileNotFoundException::class)
    fun itShouldApplyMatchForBalancedEmptyElements() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = ""
            phone = ""
            email = "jamesparker@email.com"
        }, document("2") {
            name = "James"
            address = ""
            phone = ""
            email = "jamesparker@email.com"
        })
        val result = inputData.match()
        result.forEach { println(it) }
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
        assertEquals(
            0.75, result.map { it.value }[0][0].result, 0.01
        )
    }


    @Test
    fun itShouldApplyMatchForUnBalancedEmptyElements() {
        val inputData = listOf(document("1") {
            name = "James Parker"
            address = "123 Some Street"
            phone = ""
            email = "jamesparker@email.com"
        }, document("2") {
            name = "James"
            address = ""
            phone = "123-123-1234"
            email = "jamesparker@email.com"
        })
        val result = inputData.match()
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
        assertEquals(0.625, result.map { it.value }[0][0].result, 0.01)
    }

    @Test
    fun itShouldApplyMatchWithVariance() {
        val inputData = listOf(document("1") {
            name("Tom Kelly", variance = "self")
        }, document("2") {
            name("tom kelly", variance = "self")
        })
        val result = inputData.match()
        assertEquals(2, result.size)
        result.map { it.key.key }.let {
            assertContains(it, "1")
            assertContains(it, "2")
        }
    }


    @Test
    fun itShouldApplyMatchWithDifferentVariance() {
        val inputData = listOf(document("1") {
            name("Tom Kelly", variance = "self")
        }, document("2") {
            name("tom kelly", variance = "spouse")
        })
        val result = inputData.match()
        assertEquals(0, result.size)
    }


    @Test
    fun itShouldApplyMatchWithInteger() {
        val numbers = listOf(91, 100, 200, 152, 11, 15, 10, 200)
        val documentList1 = getTestDocuments(numbers, ElementType.NUMBER, null)
        val result1 = documentList1.match()
        assertEquals(6, result1.size)
        for ((key, value) in result1) {
            val doc = key.elements.first().value as Int
            val matches = value.map { it.matchedWith.elements.first().value }
            for (matchWith in matches) {
                val match = matchWith as Int
                val small = min(doc, match)
                val big = max(doc, match)
                assertTrue(small >= 0.9 * big && small <= 1.1 * big)
            }
        }
        val documentList2 = getTestDocuments(numbers, ElementType.NUMBER, 0.99)
        val result2 = documentList2.match()
        result2.forEach(::println)
        assertEquals(2, result2.size)
    }

    @Test
    fun itShouldApplyMatchWithDoubleType() {
        val numbers = listOf(23.0, 22.0, 10.0, 5.0, 9.0, 11.0, 10.5, 23.2)
        val documentList1 = getTestDocuments(numbers, ElementType.NUMBER, null)
        val result1 = documentList1.match()
        assertEquals(6, result1.size)
        val documentList2 = getTestDocuments(numbers, ElementType.NUMBER, 0.99)
        val result2 = documentList2.match()
        assertEquals(2, result2.size)
    }

    @Test
    fun itShouldApplyMatchWithDate() {
        val dates = listOf("2020-01-01".toLocalDate(), "2020-12-01".toLocalDate(), "2020-02-01".toLocalDate())
        val documentList = getTestDocuments(dates, ElementType.DATE)
        val result = documentList.match()
        assertEquals(2, result.size)
    }

    @Test
    fun itShouldApplyMatchWithDateForHighNeighborhoodRange() {
        val dates = listOf("2020-01-01".toLocalDate(), "2020-01-02".toLocalDate(), "2019-02-01".toLocalDate())
        val documentList = getTestDocuments(dates, ElementType.DATE, 0.99) //0.99 neighborhood is about 18 days
        val result = documentList.match()
        assertEquals(2, result.size)
    }

    @Test
    fun itShouldApplyMatchWithAge() {
        val numbers = listOf(1, 2, 9, 10, 11, 45, 49, 50, 52, 55, 90, 95, 100, 107, 115)
        val documentList1 = getTestDocuments(numbers, ElementType.AGE)
        val result1 = documentList1.match()
//        documentList1[0].elements.first().value
        val collect =
            result1.entries.associate { it.key.elements.first().value to it.value.map { it.matchedWith.elements.first().value } }
        result1.forEach(::println)
        assertEquals(7, result1.size)
        collect.forEach { (key, value) ->
            for (matchWith in value) {
                val diff = abs(key as Int - matchWith as Int)
                assertTrue(diff <= 1)
            }
        }
        val documentList2 = getTestDocuments(numbers, ElementType.AGE, 0.7)
        val result2 = documentList2.match()
        val collect2 =
            result2.entries.associate { it.key.elements.first().value to it.value.map { it.matchedWith.elements.first().value } }
        assertEquals(9, result2.size)
        for ((key, value) in collect2) {
            for (matchWith in value) {
                val diff = abs(key as Int - matchWith as Int)
                assertTrue(diff <= 3)
            }
        }
    }


    private fun getTestDocuments(
        values: List<Any>, elementType: ElementType, neighborhoodRange: Double? = null
    ): List<Document> {
        var count = 0
        return values.map {
            document("${++count}") {
                element(it, elementType, neighborhoodRange = neighborhoodRange)
            }
        }
    }

    private fun getTestData(
        namePreProcessing: (String) -> String, addressPreProcessing: (String) -> String, docThreshold: Double
    ) = getCSV("test-data.csv").map { csv ->
        document(csv[0], threshold = docThreshold) {
            name(csv[1], preProcessFunction = namePreProcessing)
            address(csv[2], preProcessFunction = addressPreProcessing)
            phone = csv[3]
            email = csv[4]
        }
    }

    val testDocuments = getCSV("test-data.csv").map { csv ->
        document(csv[0], threshold = 0.5) {
            name = csv[1]
            address = csv[2]
            phone(csv[3], weight = 2, threshold = 0.5)
            email = csv[4]
        }
    }

    val demoDocuments = getCSV("demo.csv").mapIndexed { index, csv ->
        document("$index") {
            name = csv[0]
        }
    }


    companion object {
        fun getOrderedElements(elements: Set<Element<*>>): List<Element<*>> {
            return elements.sortedBy { ele -> ele.elementClassification.elementType }
        }

        fun getCSV(fileName: String) = getTestResource(fileName).let { csvReader().readAll(it) }

        /* @Throws(IOException::class)
         fun writeOutput(result: Map<String, List<Match<Document>>>) {
             val writer = CSVWriter(FileWriter("src/test/resources/output.csv"))
             writer.writeNext(arrayOf("Key", "Matched Key", "Score", "Name", "Address", "Email", "Phone"))
             result.toSortedMap().forEach { entry ->
                 val keyArrs = listOf(listOf(entry.key, entry.key, ""), getOrderedElements(
                     entry.value.map { it.data }.first().elements
                 ).map { it.value }).flatten()
                 writer.writeNext(keyArrs)
                 entry.value.stream().forEach {
                     val md = it.matchedWith
                     val matchArrs = listOf(
                         listOf("", md.key, it.result.toString()),
                         getOrderedElements(md.elements).map { it.value }).flatten()
                     writer.writeNext(matchArrs)
                 }
             }
             writer.close()
         }

         @Throws(IOException::class)
         fun writeOutput(result: Set<Set<Match<Document>>>) {
             val writer = FileWriter("src/test/resources/output.csv")
             writer.writeNext(arrayOf("Key", "Matched Key", "Score", "Name", "Address", "Email", "Phone"))
             result.forEach { matches: Set<Match<Document>> ->
                 val arr = arrayOf("Group")
                 writer.writeNext(arr)
                 matches.forEach {
                     val md = it.matchedWith
                     val matchArrs = listOf(
                         listOf("", md.key, it.result.toString()),
                         getOrderedElements(md.elements).map { it.value })
                     writer.writeNext(matchArrs)
                 }
             }
             writer.close()
         } */
    }
}