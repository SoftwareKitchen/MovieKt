package tech.softwarekitchen.moviekt.animation.once

import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTOnceAnimation

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

