package com.dogeweb.fuzzymatcher.util

actual fun getResource(resource: String): String =
    object {}.javaClass.getResource("/$resource")?.readText() ?: ""