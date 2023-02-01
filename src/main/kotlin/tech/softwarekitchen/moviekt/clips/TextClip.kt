package tech.softwarekitchen.moviekt.clips

import tech.softwarekitchen.moviekt.Vector2i
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class TextClip (base: Vector2i, size: Vector2i, text: String, fontSize: Int, color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): Clip(base,size, tOffset, visibilityDuration) {
    private val img = BufferedImage(size.x,size.y,TYPE_INT_ARGB)

    init{
        val graphics = img.createGraphics()
        graphics.font = graphics.font.deriveFont(fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,size.x,size.y)
        graphics.color = color
        graphics.drawString(text,0,2*size.y/3)
    }

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        return cloneImage(img)
    }
}