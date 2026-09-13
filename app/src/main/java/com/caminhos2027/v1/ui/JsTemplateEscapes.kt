package com.caminhos2027.v1.ui

/**
 * Keeps JavaScript template placeholders literal inside Kotlin raw strings.
 * The active-map HTML intentionally contains JS template literals such as ${zoom}.
 */
private const val zoom = "\${zoom}"
private const val x = "\${x}"
private const val y = "\${y}"

private class JsExpr(private val source: String) {
    operator fun div(value: Int): JsExpr = JsExpr("$source/$value")
    override fun toString(): String = source
}

private object window {
    val innerWidth = JsExpr("\${window.innerWidth}")
    val innerHeight = JsExpr("\${window.innerHeight}")
}
