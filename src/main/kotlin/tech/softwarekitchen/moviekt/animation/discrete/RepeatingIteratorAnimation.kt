package tech.softwarekitchen.moviekt.animation.discrete

import tech.softwarekitchen.moviekt.animation.MKTPerpetualAnimation
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
}
