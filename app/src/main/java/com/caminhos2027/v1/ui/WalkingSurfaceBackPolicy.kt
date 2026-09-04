package com.caminhos2027.v1.ui

/**
 * Defines the in-app back navigation contract independently from Android's back dispatcher.
 * The active walking surface is the root; nested APOI/decision surfaces unwind to their
 * previous surface instead of terminating the walking activity.
 */
internal object WalkingSurfaceBackPolicy {
    enum class Surface {
        ACTIVE,
        APOI_BROWSER,
        APOI_DETAIL,
        DECISION
    }

    fun target(surface: Surface): Surface? = when (surface) {
        Surface.ACTIVE -> null
        Surface.APOI_BROWSER -> Surface.ACTIVE
        Surface.APOI_DETAIL -> Surface.APOI_BROWSER
        Surface.DECISION -> Surface.ACTIVE
    }
}
