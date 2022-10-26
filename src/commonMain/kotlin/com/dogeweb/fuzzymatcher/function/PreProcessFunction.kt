package com.dogeweb.fuzzymatcher.function

import com.dogeweb.fuzzymatcher.component.Dictionary
import com.dogeweb.fuzzymatcher.util.Utils
import kotlin.jvm.JvmStatic

/**
 * A functional interface to pre-process the elements. These function are applied to element.value String's
 */
object PreProcessFunction {

    /**
     * replaces all non-numeric characters in a string
     *
     * @return the function to perform numericValue
     */
    /**
     * replaces all non-numeric characters in a string
     *
     * @return the function to perform numericValue
     */
    val numericValue: (String) -> String = { it.replace("[^0-9]".toRegex(), "") }

    /**
     * removes special characters in a string
     *
     * @return the function to perform removeSpecialChars
     */
    /**
     * removes special characters in a string
     *
     * @return the function to perform removeSpecialChars
     */
    @JvmStatic
    val removeSpecialChars: (String) -> String = { it.replace("[^A-Za-z0-9 ]+".toRegex(), "") }

    /**
     * Used for emails, remove everything after the '@' character
     *
     * @return the function to perform removeDomain
     */
    /**
     * Used for emails, remove everything after the '@' character
     *
     * @return the function to perform removeDomain
     */
    val removeDomain: (String) -> String = {
        if ("@" in it) {
            it.substring(0, it.indexOf('@'))
        }
        it
    }

    /**
     * applies both "RemoveSpecialChars" and also "addressNormalization" functions
     *
     * @return the function to perform addressPreprocessing
     */
    /**
     * applies both "RemoveSpecialChars" and also "addressNormalization" functions
     *
     * @return the function to perform addressPreprocessing
     */
    @JvmStatic
    val addressPreprocessing: (String) -> String = { removeSpecialChars(it).let(addressNormalization) }

    /**
     * applies "removeTrailingNumber", "removeSpecialChars" and "nameNormalization" functions
     *
     * @return the function to perform namePreprocessing
     */
    /**
     * applies "removeTrailingNumber", "removeSpecialChars" and "nameNormalization" functions
     *
     * @return the function to perform namePreprocessing
     */
    @JvmStatic
    val namePreprocessing: (String) -> String =
        { removeTrailingNumber(it).let(removeSpecialChars).let(nameNormalization) }

    /**
     * Uses "address-dictionary" to normalize commonly uses string in addresses
     * eg. "st.", "street", "ave", "avenue"
     *
     * @return the function to perform addressNormalization
     */
    val addressNormalization: (String) -> String =
        { Utils.getNormalizedString(it, com.dogeweb.fuzzymatcher.component.Dictionary.addressDictionary) }

    /**
     * Removes numeric character from the end of a string
     *
     * @return the function to perform removeTrailingNumber
     */
    /**
     * Removes numeric character from the end of a string
     *
     * @return the function to perform removeTrailingNumber
     */
    val removeTrailingNumber: (String) -> String = { str: String -> str.replace("\\d+$".toRegex(), "") }

    /**
     * Uses "name-dictionary" to remove common prefix and suffix in user names. like "jr", "sr", etc
     * It also removes commonly used words in company names "corp", "inc", etc
     *
     * @return the function to perform nameNormalization
     */
    /**
     * Uses "name-dictionary" to remove common prefix and suffix in user names. like "jr", "sr", etc
     * It also removes commonly used words in company names "corp", "inc", etc
     *
     * @return the function to perform nameNormalization
     */
    val nameNormalization: (String) -> String =
        { str: String -> Utils.getNormalizedString(str, com.dogeweb.fuzzymatcher.component.Dictionary.nameDictionary) }

    /**
     * For a 10 character string, it prefixes it with US international code of "1".
     *
     * @return the function to perform usPhoneNormalization
     */
    val usPhoneNormalization: (String) -> String = { numericValue(it).let { if (it.length == 10) "1$it" else it } }

    /**
     * removes all characters and retains only double numbers
     *
     * @return PreProcessFunction
     */
    @JvmStatic
    val numberPreprocessing: (Any) -> Any = {
        if (it is String) "-?\\d+(\\.\\d+)?".toRegex().find(it)?.value ?: it else none(it)
    }

    /**
     * Does nothing, used for already preprocessed values
     *
     * @return PreProcessFunction
     */
    @JvmStatic
    val none: (Any) -> Any = { it }

}