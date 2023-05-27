package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage

class SingleColorVideoClip(id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val color: Color): VideoClip(id, size,position, visible) {
    override fun renderContent(img: BufferedImage) {
        val graphics = img.createGraphics()
        graphics.color = color
        graphics.fillRect(0,0,img.width, img.height)
    }

}
