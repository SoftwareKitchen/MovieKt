package tech.softwarekitchen.moviekt.clips.video.basic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.theme.VideoTheme
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

data class ContainerBorderConfiguration(
    val width: Int = 0,
    val color: Color = Color.WHITE
)
data class ContainerVideoClipConfiguration(
    val border: ContainerBorderConfiguration = ContainerBorderConfiguration()
)

open class ContainerVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    timeShift: Float = 0f,
    private val configuration: ContainerVideoClipConfiguration = ContainerVideoClipConfiguration(),
    private val volatile: Boolean = false
): VideoClip(
    id, size, position, visible,timeShift = timeShift, volatile = volatile
) {
    private val borderColorProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_BorderColor, configuration.border.color,this::markDirty)

    init {
        registerProperty(borderColorProperty)
    }

    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        if(configuration.border.width > 0){
            val size = Vector2i(img.width, img.height)
            val graphics = img.createGraphics()
            val borderWidth = configuration.border.width.toFloat()
            graphics.stroke = BasicStroke(borderWidth)
            graphics.color = borderColorProperty.v
            graphics.drawRect((0 + borderWidth / 2).roundToInt(),(0 + borderWidth / 2).roundToInt(), (size.x - 2 * borderWidth).roundToInt(), (size.y - 2 * borderWidth).roundToInt())
        }
    }
}
