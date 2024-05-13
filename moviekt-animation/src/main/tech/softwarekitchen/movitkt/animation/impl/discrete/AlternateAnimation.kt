package tech.softwarekitchen.moviekt.animation.discrete

import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTTimerangeAnimation
import kotlin.math.floor

class AlternateAnimation<T: Any>(
    nodeId: String,
    property: String,
    start: Float,
    duration: Float,
    val frequency: Float,
    val v1: T,
    val v2: T
): MKTTimerangeAnimation<T>(
    nodeId, property, start, duration
){
    override fun get(t: Float): T {
        val rel = (t - start) * frequency
        val disc = floor(rel).toInt() % 2
        return when(disc){
            0 -> v1
            else -> v2
        }
    }

    override fun shift(t: Float): MovieKtAnimation<T> {
        return AlternateAnimation<T>(nodeId, property, start + t, duration, frequency, v1, v2)
    }
}