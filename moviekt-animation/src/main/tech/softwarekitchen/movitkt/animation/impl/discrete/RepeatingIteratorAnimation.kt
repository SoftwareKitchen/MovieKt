package tech.softwarekitchen.moviekt.animation.discrete

import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTPerpetualAnimation
import kotlin.math.floor

class RepeatingIteratorAnimation(
    nodeId: String,
    property: String,
    private val frequency: Float,
    private val limit: Int
): MKTPerpetualAnimation<Int>(nodeId, property) {
    override fun get(t: Float): Int {
        return floor(t * frequency).toInt() % limit
    }

    override fun shift(t: Float): MovieKtAnimation<Int> {
        return RepeatingIteratorAnimation(nodeId, property, frequency, limit)
    }
}
