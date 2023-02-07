package tech.softwarekitchen.moviekt.animation

import tech.softwarekitchen.common.vector.Vector2i

class DislocationAnimation(partialAnimation: (Float, Float?, Float?) -> Vector2i?): ChainableAnimation<Vector2i>(partialAnimation, Vector2i(0,0))

class DislocateAnimationFactory {
    companion object{
        fun createHorizontalShake(frequency: Float, halfAmplitude: Float): DislocationAnimation{
            return DislocationAnimation{
                tAbs, tTot, tRel ->
                val xShift = halfAmplitude * Math.sin(2 * Math.PI * frequency * tAbs)
                Vector2i(xShift.toInt(),0)
            }
        }
    }
}
