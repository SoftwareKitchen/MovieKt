package tech.softwarekitchen.moviekt.clips.video.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.min

data class ColorIndicatorVideoClipConfiguration(val color: Color, val borderColor: Color? = null, val borderWidth: Int = 3)
class ColorIndicatorVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: ColorIndicatorVideoClipConfiguration
): VideoClip(
    id, size, position, visible
) {

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val size = Vector2i(img.width, img.height)
        val a = min(size.x, size.y)
        val padding = Vector2i((size.x - a) / 2, (size.y - a) / 2)

        val bc = configuration.borderColor

        val g = img.createGraphics()
        when(bc){
            null -> {
                g.color = configuration.color
                g.fillOval(padding.x, padding.y, a, a)
            }
            else -> {
                g.color = bc
                g.fillOval(padding.x, padding.y, a, a)
                g.color = configuration.color
                g.fillOval(padding.x + configuration.borderWidth, padding.y + configuration.borderWidth, a - 2 * configuration.borderWidth, a - 2 * configuration.borderWidth)
            }
        }
    }
}
