package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.Document
import com.dogeweb.fuzzymatcher.domain.Match


/**
 *
 *
 * Starts the Matching process by element level matching and aggregates the results back
 * This uses the ScoringFunction defined at each Document to get the aggregated Document score for matched Elements
 */
class DocumentMatch(documents: List<Document> = emptyList()) {

    private val elementMatch = ElementMatch()

    init {
        documents.forEach(::add)
    }

    fun add(document: Document) {
        document.preProcessedElement.forEach(elementMatch::add)
    }

    /**
     * Executes matching of a document stream
     *
     * @param documents Stream of Document objects
     * @return Stream of Match of Document type objects
     */
    fun matchDocuments(source: List<Document>) = source.flatMap(::matchDocument)

    fun matchDocument(sourceDoc: Document, allowDuplicates: Boolean = false) = sourceDoc.preProcessedElement
        .flatMap(elementMatch::match)
        .groupBy { it.matchedWith.document }
        .run { if(!allowDuplicates) filterKeys { it != sourceDoc } else this }
        .mapNotNull {
            with(Match(sourceDoc, it.key, it.value.map { it.score })) {
                if (score.result > data.threshold) this@with else null
            }
        }

}
