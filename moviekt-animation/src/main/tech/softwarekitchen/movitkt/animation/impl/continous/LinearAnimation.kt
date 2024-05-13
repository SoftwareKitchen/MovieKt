package tech.softwarekitchen.moviekt.animation.continous

import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTTimerangeAnimation

class LinearAnimation(
    nodeId: String,
    property: String,
    start: Float,
    duration: Float,
    val from: Double = 0.0,
    val to: Double = 1.0
): MKTTimerangeAnimation<Double>(
    nodeId, property, start, duration
){
    override fun get(t: Float): Double {
        val ratio = (t - start) / duration
        return to * ratio + (1 - ratio) * from
    }

    override fun shift(t: Float): MovieKtAnimation<Double> {
        return LinearAnimation(
            nodeId,
            property,
            start + t,
            duration,
            from,
            to
        )
    }
}
