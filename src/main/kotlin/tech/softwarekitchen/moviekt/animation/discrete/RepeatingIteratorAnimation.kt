package tech.softwarekitchen.moviekt.animation.discrete

import tech.softwarekitchen.moviekt.animation.MovieKtAnimation
import kotlin.math.floor

class RepeatingIteratorAnimation(
    override val nodeId: String,
    override val property: String,
    private val frequency: Float,
    private val limit: Int
): MovieKtAnimation<Int> {
    override fun get(t: Float): Int {
        return floor(t * frequency).toInt() % limit
    }

    override fun isApplicable(t: Float): Boolean {
        return true
    }

    override fun isFinished(t: Float): Boolean {
        return false
    }
}