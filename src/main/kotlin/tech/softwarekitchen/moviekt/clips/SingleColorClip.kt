package tech.softwarekitchen.moviekt.clips

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class SingleColorClip(base: Vector2i, size: Vector2i, color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): Clip(base,size,tOffset,visibilityDuration) {
    private val image = BufferedImage(size.x,size.y,TYPE_INT_ARGB)
    init{
        val graphics = image.createGraphics()
        graphics.color = color
        graphics.fillRect(0,0,size.x,size.y)
    }

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        return cloneImage(image)
    }

}
