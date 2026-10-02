package com.caminhos2027.v1.ui

/**
 * Keeps JavaScript template placeholders literal inside Kotlin raw strings.
 * The active-map HTML intentionally contains JS template literals such as ${zoom}.
 */
internal const val zoom = "\${zoom}"
internal const val x = "\${x}"
internal const val y = "\${y}"

internal class JsExpr(private val source: String) {
    operator fun div(value: Int): JsExpr = JsExpr("$source/$value")
    override fun toString(): String = source
}

internal object window {
    val innerWidth = JsExpr("\${window.innerWidth}")
    val innerHeight = JsExpr("\${window.innerHeight}")
}
