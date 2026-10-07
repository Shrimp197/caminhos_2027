package com.caminhos2027.v1.gps

/** Source of raw positions. Both real Android GPS and QA simulation feed the same walking pipeline. */
interface LocationSource {
    fun start()
    fun stop()
}
