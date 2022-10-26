package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.domain.Document
import com.dogeweb.fuzzymatcher.domain.Match

/**
 * Use this for De-duplication of data, where for a given list of documents it finds duplicates
 * Data is aggregated by a given Document
 *
 * @param this@applyMatch the list of documents to match against
 * @return a map containing the grouping of each document and its corresponding matches
 */
fun List<Document>.match(): Map<Document, List<Match<Document>>> = privateMatch(this).groupBy { it.data }

private fun Set<Match<Document>>.containsMatch(match: Match<Document>): Boolean {
    return any { it.data.key == match.matchedWith.key && it.matchedWith.key == match.data.key }
}

private fun groupSimilar(
    matchMap: MutableMap<String, List<Match<Document>>>, key: String, matchGroups: MutableSet<Match<Document>>
) {
    val matches = matchMap[key] ?: return
    matchMap.remove(key)
    matches.forEach { match: Match<Document> ->
        if (!matchGroups.containsMatch(match)) {
            matchGroups.add(match)
        }
        val matchedWithKey = match.matchedWith.key
        groupSimilar(matchMap, matchedWithKey, matchGroups)
    }
}

/**
 * Use this for De-duplication of data, where for a given list of documents it finds duplicates
 * Data is aggregated by a given Document Id
 *
 * @param this@applyMatchByGroups the list of documents to match against
 * @return a set containing the grouping of all relevant matches. So if A matches B, and B matches C. They will be grouped together
 */
fun List<Document>.matchWithByGroups(): Set<Set<Match<Document>>> {
    val documentMatch = DocumentMatch(this)
    val matchByKey = documentMatch.matchDocuments(this).groupBy { it.data.key }.toMutableMap()
    val docKeys = HashSet(matchByKey.keys)
    val result: MutableSet<Set<Match<Document>>> = HashSet()
    docKeys.forEach {
        val matchGroups = hashSetOf<Match<Document>>()
        groupSimilar(matchByKey, it, matchGroups)
        if (matchGroups.isNotEmpty()) result.add(matchGroups)
    }
    return result
}

/**
 * Use this to check duplicates for bulk inserts, where a list of new Documents is checked against existing list
 * Data is aggregated by a given Document Id
 *
 * @param this@applyMatchByDocId the list of documents to match from
 * @param other the list of documents to match against
 * @return a map containing the grouping of each document id and its corresponding matches
 */
infix fun List<Document>.matchWithById(other: List<Document>) =
    privateMatch(this, other).groupBy { it.data.key }

/**
 * Use this for De-duplication of data, where for a given list of documents it finds duplicates
 * Data is aggregated by a given Document Id
 *
 * @param this@applyMatchByDocId the list of documents to match against
 * @return a map containing the grouping of each document id and its corresponding matches
 */
fun List<Document>.matchById(): Map<String, List<Match<Document>>> = privateMatch(this).groupBy { it.data.key }

/**
 * Use this to check duplicate for a new record, where it checks whether a new Document is a duplicate in existing list
 * Data is aggregated by a given Document Id
 *
 * @param this@applyMatchByDocId  the document to match
 * @param matchWith the list of documents to match against
 * @return a map containing the grouping of each document id and its corresponding matches
 */
//infix fun Document.matchWithById(matchWith: List<Document>): Map<String, List<Match<Document>>> {
//    return privateMatch(this, other)
//}

/**
 * Use this to check duplicate for a new record, where it checks whether a new Document is a duplicate in existing list
 * Data is aggregated by a given Document
 *
 * @param this@applyMatch  the document to match
 * @param other the list of documents to match against
 * @return a map containing the grouping of each document and its corresponding matches
 */
infix fun Document.matchWith(other: List<Document>): List<Match<Document>> {
    return privateMatch(this, other)
}

/**
 * Use this to check duplicates for bulk inserts, where a list of new Documents is checked against existing list
 * Data is aggregated by a given Document
 *
 * @param this@applyMatch the list of documents to match from
 * @param other the list of documents to match against
 * @return a map containing the grouping of each document and its corresponding matches
 */
infix fun List<Document>.matchWith(other: List<Document>): Map<Document, List<Match<Document>>> {
    return privateMatch(this, other).groupBy { it.data }
}

private fun privateMatch(source: List<Document>): List<Match<Document>> {
    return DocumentMatch(source).matchDocuments(source)
}

private fun privateMatch(source: List<Document>, other: List<Document>): List<Match<Document>> {
    return DocumentMatch(other).matchDocuments(source)
}

private fun privateMatch(source: Document, other: List<Document>): List<Match<Document>> {
    return DocumentMatch(other).matchDocument(source)
}
