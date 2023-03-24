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
    size: SizeProvider,
    imageFile: File,
    private val configuration: StaticImageVideoClipConfiguration = StaticImageVideoClipConfiguration(),
    tOffset: Float = 0f,
    visibilityDuration: Float? = null
): VideoClip(size, tOffset, visibilityDuration) {
    val toDraw = ImageIO.read(imageFile)

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage(frameNo, nFrames, tTotal)

        val graphics = img.createGraphics()
        when(configuration.mode){
            StaticImageMode.KeepSize -> graphics.drawImage(toDraw,0,0,null)
            StaticImageMode.Stretch -> graphics.drawImage(toDraw.getScaledInstance(img.width,img.height, Image.SCALE_SMOOTH),0,0,null)
        }

        return img
    }
}