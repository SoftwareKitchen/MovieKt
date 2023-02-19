package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageSlideshowVideoClip(
    size: SizeProvider,
    private val imageFiles: List<File>,
    private val timePerImage: Float,
    private val repeat: Boolean = false,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
): VideoClip(size, tOffset, visibilityDuration) {
    private val images = imageFiles.map{ImageIO.read(it)}

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)
        val imgIndex = when(repeat){
            true -> Math.floor(tTotal.toDouble() / timePerImage).toInt() % images.size
            false -> Math.floor(tTotal.toDouble() / timePerImage).toInt()
        }

        if(imgIndex < 0 || imgIndex >= images.size){
            return img
        }

        val graphics = img.createGraphics()
        graphics.drawImage(images[imgIndex], 0, 0, null)

        return img
    }
}
