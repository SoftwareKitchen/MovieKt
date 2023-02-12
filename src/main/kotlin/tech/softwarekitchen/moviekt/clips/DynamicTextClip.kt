package tech.softwarekitchen.moviekt.clips

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class DynamicTextClip (base: Vector2i, size: Vector2i, private val text: (Int, Int, Float) -> String, private val fontSize: Int, private val color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): Clip(base,size, tOffset, visibilityDuration) {

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        val img = BufferedImage(size.x, size.y, TYPE_INT_ARGB)

        val graphics = img.createGraphics()
        graphics.font = graphics.font.deriveFont(fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,size.x,size.y)
        graphics.color = color
        graphics.drawString(text(frameNo, nFrames, tInternal),0,2*size.y/3)

        return img
    }
}
