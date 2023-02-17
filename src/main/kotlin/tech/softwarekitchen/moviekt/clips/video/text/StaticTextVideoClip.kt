package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class StaticTextVideoClip (size: Vector2i, text: String, fontSize: Int, color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size, tOffset, visibilityDuration) {
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