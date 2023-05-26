package tech.softwarekitchen.moviekt.animation.switchanim

import tech.softwarekitchen.moviekt.animation.MKTTimerangeAnimation

class SetWithResetAnimation(
    nodeId: String,
    property: String,
    setAt: Float,
    resetAt: Float
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
}
