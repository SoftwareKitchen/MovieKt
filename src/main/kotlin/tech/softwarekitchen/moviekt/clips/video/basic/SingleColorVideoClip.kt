package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class SingleColorVideoClip(id: String, size: Vector2i, position: Vector2i, private val color: Color): VideoClip(id, size,position) {
    override fun renderContent(img: BufferedImage) {
        val graphics = img.createGraphics()
        graphics.color = color
        graphics.fillRect(0,0,img.width, img.height)
    }

}
