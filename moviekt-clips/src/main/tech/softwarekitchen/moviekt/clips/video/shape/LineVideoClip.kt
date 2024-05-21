package tech.softwarekitchen.moviekt.clips.video.shape

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage

data class LineVideoClipConfiguration(
    val from: Vector2i,
    val to: Vector2i,
    val color: Color = Color.WHITE
)

class LineVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: LineVideoClipConfiguration
): VideoClip(id, size, position, visible) {

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val g = img.graphics
        g.color = configuration.color
        g.drawLine(
            configuration.from.x,
            configuration.from.y,
            configuration.to.x,
            configuration.to.y
        )
    }
}
