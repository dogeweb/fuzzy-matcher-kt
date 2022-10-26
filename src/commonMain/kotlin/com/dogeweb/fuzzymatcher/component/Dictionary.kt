package com.dogeweb.fuzzymatcher.component

import com.dogeweb.fuzzymatcher.util.getResource
import kotlin.jvm.JvmField

/**
 * Used for Pre-Processing, the Dictionary caches a pre-defined normalization and replacement for common characters
 * found in names and adresses.
 *
 */
object Dictionary {
//    private val LOGGER = LoggerFactory.getLogger(Dictionary::class.java)

    @JvmField
    val addressDictionary =
        com.dogeweb.fuzzymatcher.component.Dictionary.getDictionary(getResource("address-dictionary.txt"))

    @JvmField
    val nameDictionary = com.dogeweb.fuzzymatcher.component.Dictionary.getDictionary(getResource("name-dictionary.txt"))

    private fun getDictionary(string: String): Map<String, String> {
        return string.lineSequence().map { it.lowercase().split(":".toRegex(), limit = 2) }
            .associateBy({ it[0].trim { it <= ' ' } }, { it[1].trim { it <= ' ' } })
    }
}