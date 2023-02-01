package tech.softwarekitchen.moviekt.clips

import tech.softwarekitchen.moviekt.Vector2i
import java.awt.image.BufferedImage
import java.lang.Float.max
import java.lang.Float.min

abstract class Clip(
    private val base: Vector2i,
    val size: Vector2i,
    val tOffset: Float,
    val visibilityDuration: Float?
){
    private val children =  ArrayList<Clip>()
    private var dislocate: (Float, Float?, Float?) -> Vector2i = {tAbs, tTot, tRel -> Vector2i(0,0)}
    private var opacity: (Float, Float?, Float?) -> Float = {tAbs, tTot, tRel -> 1f}
    abstract fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage

    fun addChild(child: Clip){
        children.add(child)
    }

    fun setOpacity(func: (Float, Float?, Float?) -> Float){
        opacity = func
    }

    fun setDislocate(func: (Float, Float?, Float?) -> Vector2i){
        dislocate = func
    }

    fun getPosition(tAbs: Float, tTot: Float?, tRel: Float?): Vector2i{
        return base.plus(dislocate(tAbs, tTot, tRel))
    }

    fun render(frameNo: Int, nFrames: Int, t: Float): BufferedImage{
        val tInternal = t - tOffset
        val background = renderContent(frameNo, nFrames, t, tInternal)

        val tFac = when(visibilityDuration){
            null -> null
            else -> tInternal / visibilityDuration
        }

        children
            .filter{t >= it.tOffset && (it.visibilityDuration == null || t <= it.tOffset+it.visibilityDuration)}
            .forEach{
                child ->
                val childImg = child.render(frameNo, nFrames, tInternal)
                val childPosition = child.getPosition(tInternal,visibilityDuration,tFac)
                background.graphics.drawImage(childImg,childPosition.x,childPosition.y,child.size.x,child.size.y,null)
            }

        val copy = cloneImage(background)
        val alpha = opacity(tInternal, visibilityDuration, tFac)

        if(alpha < 1f) {
            for (x in 0 until copy.width) {
                for (y in 0 until copy.height) {
                    val argb = copy.getRGB(x, y).toUInt()
                    val rgb = argb % 16777216u
                    val a = argb / 16777216u
                    val aCorrected = min(max(a.toFloat() * alpha, 0f), 255f).toUInt()
                    val argbCorrected = aCorrected * 16777216u + rgb
                    copy.setRGB(x,y,argbCorrected.toInt())
                }
            }
        }

        return copy
    }

    protected fun cloneImage(src: BufferedImage): BufferedImage{
        return BufferedImage(src.colorModel, src.copyData(null),src.isAlphaPremultiplied,null)
    }
}

