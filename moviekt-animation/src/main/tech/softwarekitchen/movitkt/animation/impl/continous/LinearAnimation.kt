package tech.softwarekitchen.moviekt.animation.continous

import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.movitkt.animation.MovieKtAnimation
import tech.softwarekitchen.movitkt.animation.basic.MKTTimerangeAnimation
import kotlin.math.cos

interface Interpolatable<T: Any>{
    fun interpolateLinear(f: Float): T
}

class DoubleInterpolator(val f1: Double, val f2: Double): Interpolatable<Double>{
    override fun interpolateLinear(f: Float): Double {
        return (1.0 - f) * f1 + f * f2
    }
}

class Vector2Interpolator(val f1: Vector2, val f2: Vector2): Interpolatable<Vector2>{
    override fun interpolateLinear(f: Float): Vector2 {
        return f1.scale(1.0 - f).plus(f2.scale(f.toDouble()))
    }
}

class Vector2iInterpolator(val f1: Vector2i, val f2: Vector2i): Interpolatable<Vector2i>{
    override fun interpolateLinear(f: Float): Vector2i {
        return f1 * (1.0 - f) + f2 * f.toDouble()
    }
}

interface AnimationFolder{
    fun fold(orig: Float): Float
}

val AcosEaseFolder = object:AnimationFolder{
    override fun fold(orig: Float): Float {
        val angle = Math.PI * orig
        val res = (.5f - cos(angle) / 2f).toFloat()
        return res
    }
}

class LinearAnimation<T: Any>(
    nodeId: String,
    property: String,
    start: Float,
    duration: Float,
    private val interpolator: Interpolatable<T>,
    private val folder: AnimationFolder? = null
): MKTTimerangeAnimation<T>(
    nodeId, property, start, duration
){

    override fun get(t: Float): T {
        val orig = (t - start) / duration
        val folded = folder?.fold(orig) ?: orig
        return interpolator.interpolateLinear(folded)
    }

    override fun shift(t: Float): MovieKtAnimation<T> {
        return LinearAnimation(
            nodeId,
            property,
            start + t,
            duration,
            interpolator,
            folder
        )
    }
}
