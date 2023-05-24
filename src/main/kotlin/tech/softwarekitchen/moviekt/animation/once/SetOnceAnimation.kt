package tech.softwarekitchen.moviekt.animation.once

import tech.softwarekitchen.moviekt.animation.MovieKtAnimation

class SetOnceAnimation<T: Any>(
    override val nodeId: String,
    override val property: String,
    private val at: Float,
    private val value: T
): MovieKtAnimation<T> {
    override fun get(t: Float): T {
        return value
    }

    override fun isApplicable(t: Float): Boolean {
        return t >= at
    }

    override fun isFinished(t: Float): Boolean {
        return t >= at
    }
}

