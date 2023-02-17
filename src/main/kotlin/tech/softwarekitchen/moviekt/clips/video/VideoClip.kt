package tech.softwarekitchen.moviekt.clips.video

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.image.BufferedImage
import java.lang.Float.max
import java.lang.Float.min

abstract class VideoClip(
    val size: Vector2i,
    val tOffset: Float,
    val visibilityDuration: Float?
){
    private val children =  ArrayList<Pair<VideoClip, (Int, Int, Float) -> Vector2i>>()
    private var opacity: (Float, Float?, Float?) -> Float = {tAbs, tTot, tRel -> 1f}
    abstract fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage

    fun addChild(child: VideoClip, position: Vector2i){
        addChild(child) { _, _, _ -> position }
    }
    fun addChild(child: VideoClip, position: (Int, Int, Float) -> Vector2i){
        children.add(Pair(child, position))
    }

    fun setOpacity(func: (Float, Float?, Float?) -> Float){
        opacity = func
    }

    fun render(frameNo: Int, nFrames: Int, t: Float): BufferedImage{
        val tInternal = t - tOffset
        val background = renderContent(frameNo, nFrames, t, tInternal)

        val tFac = when(visibilityDuration){
            null -> null
            else -> tInternal / visibilityDuration
        }

        children
            .filter{t >= it.first.tOffset && (it.first.visibilityDuration == null || t <= it.first.tOffset+it.first.visibilityDuration!!)}
            .forEach{
                child ->
                val childImg = child.first.render(frameNo, nFrames, tInternal)
                val childPosition = child.second(frameNo, nFrames, t)
                background.graphics.drawImage(childImg,childPosition.x,childPosition.y,child.first.size.x,child.first.size.y,null)
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

