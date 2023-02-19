package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class StaticImageVideoClip(
    size: SizeProvider,
    imageFile: File,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
): VideoClip(size, tOffset, visibilityDuration) {
    val toDraw = ImageIO.read(imageFile)

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)
        val graphics = img.createGraphics()
        graphics.drawImage(toDraw,0,0,null)
        return img
    }
}