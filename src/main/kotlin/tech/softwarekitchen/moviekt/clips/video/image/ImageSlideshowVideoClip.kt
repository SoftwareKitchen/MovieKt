package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
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

    private val images = imageFiles.map{ImageIO.read(it)}
    private val imageIndexProperty = VideoClipProperty(PropertyKey_ImageIndex, 0, this::markDirty)
    init{
        registerProperty(imageIndexProperty)
    }

    override fun renderContent(img: BufferedImage) {
        val imgIndex = imageIndexProperty.v

        if(imgIndex < 0 || imgIndex >= images.size){
            return
        }

        val graphics = img.createGraphics()
        graphics.drawImage(images[imgIndex], 0, 0, null)
    }
}
