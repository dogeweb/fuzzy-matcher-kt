package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.Document
import com.dogeweb.fuzzymatcher.domain.document
import java.io.FileNotFoundException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.test.Test

/**
 *
 */
class MatchServicePerfTest {

    @Test
    fun itShouldApplyMatchForBigData() {
//        (500..500 step 500).forEach {
//            applyMatch(bigDataDocuments.take(it))
//        }
        applyMatch(bigDataDocuments.take(6000))
    }

    @Test

    /*
     * 2000 - 1.41 G
     * 4000 - 2.45 G
     * 6000 - 4.01 G
     *
     */
    @Throws(FileNotFoundException::class)
    fun itShouldApplyMatchForBigDataForMemoryPerf() {
        val docSize = 6000
        val leftDoc = bigDataDocuments.take(docSize)
        val rightDoc = bigDataDocuments.take(docSize)
        recordMemoryUsage({ applyMatch(leftDoc, rightDoc) }, 10)
    }

    private fun applyMatch(documentList: List<Document>) {
        val startTime = System.nanoTime()
        val result = documentList.matchById()
        val endTime = System.nanoTime()
        //Assert.assertEquals(116, result.size());
        val duration = (endTime - startTime) / 1000000
        println("Execution time (ms) for + " + documentList.size * ELEM_PER_DOC + " count : " + duration)
        println()
    }

    @get:Throws(FileNotFoundException::class)
    val bigDataDocuments: List<Document>
        get() {
            var index = 0
            return MatchServiceTest.getCSV("Sample-Big-Data.csv").map {
                   document("${++index}") {
                        name = it[0]
                        address = getAddress(it)
                        phone = it[5]
                        email = it[6]
                    }
                }
        }

    private fun applyMatch(left: List<Document>, right: List<Document>) {
        val startTime = System.nanoTime()
        val result = left matchWithById right
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1000000
        println("Execution time (ms) for transactions : $duration")
    }

    fun recordMemoryUsage(runnable: Runnable?, runTimeSecs: Int) {
        try {
            val mainProcessFuture = CompletableFuture.runAsync(runnable)
            val memUsageFuture = CompletableFuture.runAsync {
                var mem: Long = 0
                for (cnt in 0 until runTimeSecs) {
                    val memUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                    mem = if (memUsed > mem) memUsed else mem
                    try {
                        TimeUnit.SECONDS.sleep(1)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                println("Max memory used (gb): " + mem / 1000000000.0)
            }
            val allOf = CompletableFuture.allOf(mainProcessFuture, memUsageFuture)
            allOf.get()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val ELEM_PER_DOC = 4
        fun getAddress(csv: List<String>) = (1..4).joinToString(" ") { csv[it] }
    }
}