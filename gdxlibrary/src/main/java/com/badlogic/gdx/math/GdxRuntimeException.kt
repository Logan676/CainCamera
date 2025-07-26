package com.badlogic.gdx.math

/** Typed runtime exception used throughout libgdx */
class GdxRuntimeException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
