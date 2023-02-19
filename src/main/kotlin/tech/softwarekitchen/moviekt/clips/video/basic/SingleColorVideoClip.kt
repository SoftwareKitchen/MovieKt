package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class SingleColorVideoClip(size: SizeProvider, private val color: Color, tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size,tOffset,visibilityDuration) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val image = generateEmptyImage(frameNo, nFrames, tTotal)

        val graphics = image.createGraphics()
        graphics.color = color
        graphics.fillRect(0,0,size(frameNo, nFrames, tTotal).x,size(frameNo, nFrames, tTotal).y)

        return cloneImage(image)
    }

}
