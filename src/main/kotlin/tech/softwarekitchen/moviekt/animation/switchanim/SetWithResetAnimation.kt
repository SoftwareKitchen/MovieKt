package tech.softwarekitchen.moviekt.animation.switchanim

import tech.softwarekitchen.moviekt.animation.MovieKtAnimation

class SetWithResetAnimation(
    override val nodeId: String,
    override val property: String,
    private val setAt: Float,
    private val resetAt: Float
): MovieKtAnimation<Boolean> {
    override fun isFinished(t: Float): Boolean {
        return t >= resetAt
    }

    override fun get(t: Float): Boolean {
        return when{
            t < setAt -> false
            t < resetAt -> true
            else -> false
        }
    }

    override fun isApplicable(t: Float): Boolean {
        return t >= setAt
    }
}
