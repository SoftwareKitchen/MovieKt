package tech.softwarekitchen.moviekt.animation.continous

import tech.softwarekitchen.moviekt.animation.MKTTimerangeAnimation
import kotlin.math.PI
import kotlin.math.sin

class SineAnimation(
    nodeId: String,
    property: String,
    start: Float,
    duration: Float,
    val offset: Double,
    val halfAmp: Double,
    val frequency: Double
): MKTTimerangeAnimation<Double>(
    nodeId, property, start, duration
){
    override fun get(t: Float): Double {
        val off = t - start
        val angle = 2 * PI * frequency * off
        return offset + halfAmp * sin(angle)
    }
}
