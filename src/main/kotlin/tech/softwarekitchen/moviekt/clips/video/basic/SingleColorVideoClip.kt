package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class SingleColorVideoClip(size: Vector2i, color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size,tOffset,visibilityDuration) {
    private val image = BufferedImage(size.x,size.y,TYPE_INT_ARGB)
    init{
        val graphics = image.createGraphics()
        graphics.color = color
        graphics.fillRect(0,0,size.x,size.y)
    }

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        return cloneImage(image)
    }

}
