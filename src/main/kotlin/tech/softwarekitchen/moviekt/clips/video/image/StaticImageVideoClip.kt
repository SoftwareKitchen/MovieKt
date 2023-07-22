package tech.softwarekitchen.moviekt.clips.video.image

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.min

enum class StaticImageMode{
    KeepSize, Stretch, StretchWithAspect
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

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()
        when(configuration.mode){
            StaticImageMode.KeepSize -> graphics.drawImage(toDraw,0,0,null)
            StaticImageMode.Stretch -> graphics.drawImage(toDraw.getScaledInstance(img.width,img.height, Image.SCALE_SMOOTH),0,0,null)
            StaticImageMode.StretchWithAspect -> {
                val aspectX = getSize().x.toDouble() / toDraw.width
                val aspectY = getSize().y.toDouble() / toDraw.height
                val aspect = min(aspectX, aspectY)
                val paddingX = (getSize().x - aspect * toDraw.width) / 2
                val paddingY = (getSize().y - aspect * toDraw.height) / 2
                val scaled = toDraw.getScaledInstance((toDraw.width * aspect).toInt(),(toDraw.height * aspect).toInt(),Image.SCALE_SMOOTH)
                graphics.drawImage(scaled,paddingX.toInt(), paddingY.toInt(),null)
            }
        }
    }
}