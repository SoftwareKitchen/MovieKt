package tech.softwarekitchen.moviekt.animation.continous

import tech.softwarekitchen.moviekt.animation.MovieKtAnimation

class LinearAnimation(
    override val nodeId: String,
    override val property: String,
    val start: Float,
    val duration: Float,
    val from: Double = 0.0,
    val to: Double = 1.0
): MovieKtAnimation<Double> {
    override fun isFinished(t: Float): Boolean {
        return t > start + duration
    }

    override fun isApplicable(t: Float): Boolean {
        return t >= start && !isFinished(t)
    }

    override fun get(t: Float): Double {
        val ratio = (t - start) / duration
        return to * ratio + (1 - ratio) * from
    }
}
