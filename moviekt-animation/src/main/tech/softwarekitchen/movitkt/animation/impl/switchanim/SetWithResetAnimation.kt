package tech.softwarekitchen.moviekt.animation.switchanim

import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTTimerangeAnimation

class SetWithResetAnimation(
    nodeId: String,
    property: String,
    private val setAt: Float,
    private val resetAt: Float
): MKTTimerangeAnimation<Boolean>(
    nodeId, property, setAt, resetAt - setAt
) {
    override fun get(t: Float): Boolean {
        return when{
            t < start -> false
            t < start + duration -> true
            else -> false
        }
    }

    override fun shift(t: Float): MovieKtAnimation<Boolean> {
        return SetWithResetAnimation(nodeId,property, setAt + t, resetAt + t)
    }
}
