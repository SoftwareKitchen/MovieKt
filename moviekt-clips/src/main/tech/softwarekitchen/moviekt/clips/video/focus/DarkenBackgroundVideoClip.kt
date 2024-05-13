package tech.softwarekitchen.moviekt.clips.video.focus

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage

data class DarkenBackgroundVideoClipConfiguration(
    val base: Vector2i,
    val windowSize: Vector2i,
    val opacity: Double = 0.5

)

class DarkenBackgroundVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val configuration: DarkenBackgroundVideoClipConfiguration
): VideoClip(id, size, position, visible) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
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
