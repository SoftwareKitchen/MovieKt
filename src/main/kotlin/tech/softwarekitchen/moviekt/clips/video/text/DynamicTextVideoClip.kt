package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class DynamicTextVideoClip (size: SizeProvider, private val text: (Int, Int, Float) -> String, private val fontSize: Int, private val color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size, tOffset, visibilityDuration) {

    override fun renderContent(frameNo: Int, nFrames: Int, t: Float): BufferedImage {
        val curSize = size(frameNo, nFrames, t)
        val img = BufferedImage(curSize.x, curSize.y, TYPE_INT_ARGB)

        val graphics = img.createGraphics()
        graphics.font = graphics.font.deriveFont(fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,curSize.x,curSize.y)
        graphics.color = color
        graphics.drawString(text(frameNo, nFrames, t),0,2*curSize.y/3)

        return img
    }
}
