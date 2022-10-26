package com.dogeweb.fuzzymatcher.exception

class MatchException : RuntimeException {
    constructor() : super() {}
    constructor(message: String?) : super(message) {}
    constructor(t: Throwable?) : super(t) {}
    constructor(message: String?, t: Throwable?) : super(message, t) {}
}