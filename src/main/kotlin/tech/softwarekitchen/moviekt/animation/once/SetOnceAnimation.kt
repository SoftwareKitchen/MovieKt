package tech.softwarekitchen.moviekt.animation.once

import tech.softwarekitchen.moviekt.animation.MKTOnceAnimation
import tech.softwarekitchen.moviekt.animation.MovieKtAnimation

class SetOnceAnimation<T: Any>(
    nodeId: String,
    property: String,
    at: Float,
    private val value: T
): MKTOnceAnimation<T>(
    nodeId, property, at
) {
    override fun get(t: Float): T {
        return value
    }

    override fun shift(t: Float): MovieKtAnimation<T> {
        return SetOnceAnimation(nodeId, property, at + t, value)
    }
}

