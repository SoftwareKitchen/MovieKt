package tech.softwarekitchen.moviekt.clips.video.image

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class ImageSlideshowVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val imageFiles: List<File>,
): VideoClip(id, size, position, visible) {
    companion object{
        val PropertyKey_ImageIndex = "ImageIndex"
    }

    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val images = imageFiles.map{ImageIO.read(it)}
    private val imageIndexProperty = VideoClipProperty(PropertyKey_ImageIndex, 0, this::markDirty)
    init{
        registerProperty(imageIndexProperty)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val imgIndex = imageIndexProperty.v

        if(imgIndex < 0 || imgIndex >= images.size){
            return
        }

        val graphics = img.createGraphics()
        graphics.drawImage(images[imgIndex], 0, 0, null)
    }
}
