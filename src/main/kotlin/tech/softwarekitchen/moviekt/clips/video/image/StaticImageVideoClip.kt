package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class StaticImageVideoClip(
    size: Vector2i,
    imageFile: File,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
): VideoClip(size, tOffset, visibilityDuration) {
    private val cachedImage = generateEmptyImage()
    init{
        val image = ImageIO.read(imageFile)
        val graphics = cachedImage.createGraphics()
        graphics.drawImage(image,0,0,null)
    }

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        return cloneImage(cachedImage)
    }
}