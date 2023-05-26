package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

enum class StaticImageMode{
    KeepSize, Stretch
}
data class StaticImageVideoClipConfiguration(
    val mode: StaticImageMode = StaticImageMode.KeepSize
)

class StaticImageVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    imageFile: File,
    private val configuration: StaticImageVideoClipConfiguration = StaticImageVideoClipConfiguration(),
): VideoClip(id, size, position, visible) {
    val toDraw = ImageIO.read(imageFile)

    override fun renderContent(img: BufferedImage) {
        val graphics = img.createGraphics()
        when(configuration.mode){
            StaticImageMode.KeepSize -> graphics.drawImage(toDraw,0,0,null)
            StaticImageMode.Stretch -> graphics.drawImage(toDraw.getScaledInstance(img.width,img.height, Image.SCALE_SMOOTH),0,0,null)
        }
    }
}