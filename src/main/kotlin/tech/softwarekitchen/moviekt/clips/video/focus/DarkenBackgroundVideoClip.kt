package tech.softwarekitchen.moviekt.clips.video.focus

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

data class DarkenBackgroundVideoClipConfiguration(
    val base: Vector2i,
    val windowSize: Vector2i,
    val opacity: Double = 0.5

)

class DarkenBackgroundVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val configuration: DarkenBackgroundVideoClipConfiguration
): VideoClip(id, size, position, visible) {
    override fun renderContent(img: BufferedImage) {
        val currentSize = Vector2i(img.width, img.height)

        val graphics = img.createGraphics()
        graphics.color = Color(0,0,0,(255 * configuration.opacity).toInt())
        graphics.fillRect(0,0,currentSize.x, currentSize.y)

        graphics.fillRect(0,0,currentSize.x,configuration.base.y)
        graphics.fillRect(0,configuration.base.y + configuration.windowSize.y, currentSize.x, currentSize.y - configuration.base.y - configuration.windowSize.y)
        graphics.fillRect(0,configuration.base.y, configuration.base.x,configuration.windowSize.y)
        graphics.fillRect(configuration.base.x + configuration.windowSize.x, configuration.base.y, currentSize.x - configuration.base.x - configuration.windowSize.x, configuration.windowSize.y)
    }
}
