package com.dogeweb.fuzzymatcher.util

object Utils {
    fun getNGrams(value: String, size: Int): List<String> {
/*        val stringStream = mutableListOf<String>()
        if (value.length <= size) {
            stringStream.add(value)
        } else {
            val nGramTokenizer = NGramTokenizer(size, size)
            val charTermAttribute: CharTermAttribute = nGramTokenizer.addAttribute(CharTermAttribute::class.java)
            nGramTokenizer.setReader(StringReader(value))
            try {
                nGramTokenizer.reset()
                while (nGramTokenizer.incrementToken()) {
                    stringStream.add(charTermAttribute.toString())
                }
                nGramTokenizer.end()
                nGramTokenizer.close()
            } catch (io: IOException) {
                throw MatchException("Failure in creating tokens : ", io)
            }
        }
        return stringStream*/
        if(value.length <= size) {
            return listOf(value)
        }
        val ret = mutableListOf<String>()
        for (i in 0 .. value.length - size){
            ret.add(value.substring(i, i + size))
        }
        return ret
    }

    /**
     * utility method to apply dictionary for normalizing strings
     *
     * @param str A String of element value to be nomalized
     * @param dict A dictonary map containing the mapping of string to normalize
     * @return the normalized string
     */
    fun getNormalizedString(str: String, dict: Map<String, String>): String {
        return str.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }
            .map { d: String -> if (dict.containsKey(d.lowercase())) dict[d.lowercase()] else d }
            .joinToString(" ")
    }

    fun isNumeric(str: String): Boolean {
        return str.matches(".*\\d.*".toRegex())
    }
}