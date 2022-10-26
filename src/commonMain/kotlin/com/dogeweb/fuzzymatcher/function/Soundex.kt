package com.dogeweb.fuzzymatcher.function

class Soundex {

    @get:Deprecated("")
    @set:Deprecated("")
    @Deprecated("")
    var maxLength = 4
    private val soundexMapping: CharArray
    private val specialCaseHW: Boolean

    constructor() {
        soundexMapping = US_ENGLISH_MAPPING
        specialCaseHW = true
    }

    private fun hasMarker(mapping: CharArray): Boolean {
        val var3 = mapping.size
        for (var4 in 0 until var3) {
            val ch = mapping[var4]
            if (ch == '-') {
                return true
            }
        }
        return false
    }

    constructor(mapping: String) {
        soundexMapping = mapping.toCharArray()
        specialCaseHW = !hasMarker(soundexMapping)
    }

    constructor(mapping: String, specialCaseHW: Boolean) {
        soundexMapping = mapping.toCharArray()
        this.specialCaseHW = specialCaseHW
    }

    fun encode(str: String): String {
        return soundex(str)
    }

    private fun map(ch: Char): Char {
        val index = ch.code - 65
        return if (index >= 0 && index < soundexMapping.size) {
            soundexMapping[index]
        } else {
//            throw IllegalArgumentException("The character is not mapped: $ch (index=$index)")
            ' '
        }
    }

    private fun clean(str: String): String {
        return if (str.isNotEmpty()) {
            val len = str.length
            val chars = CharArray(len)
            var count = 0
            for (i in 0 until len) {
                if (str[i].isLetter()) {
                    chars[count++] = str[i]
                }
            }
            if (count == len) {
                str.uppercase()
            } else {
                chars.concatToString(0, 0 + count).uppercase()
            }
        } else {
            str
        }
    }

    private fun soundex(string: String): String {
        var str = string
        return run {
            str = clean(str)
            if (str.isEmpty()) {
                str
            } else {
                val out = charArrayOf('0', '0', '0', '0')
                var count = 0
                val first = str[0]
                out[count++] = first
                var lastDigit = this.map(first)
                var i = 1
                while (i < str.length && count < out.size) {
                    val ch = str[i]
                    if (!specialCaseHW || ch != 'H' && ch != 'W') {
                        val digit = this.map(ch)
                        if (digit != '-') {
                            if (digit != '0' && digit != lastDigit) {
                                out[count++] = digit
                            }
                            lastDigit = digit
                        }
                    }
                    ++i
                }
                out.concatToString()
            }
        }
    }

    companion object {
        const val SILENT_MARKER = '-'
        const val US_ENGLISH_MAPPING_STRING = "01230120022455012623010202"
        private val US_ENGLISH_MAPPING = "01230120022455012623010202".toCharArray()
        val US_ENGLISH = Soundex()
        val US_ENGLISH_SIMPLIFIED = Soundex("01230120022455012623010202", false)
        val US_ENGLISH_GENEALOGY = Soundex("-123-12--22455-12623-1-2-2")
    }
}
