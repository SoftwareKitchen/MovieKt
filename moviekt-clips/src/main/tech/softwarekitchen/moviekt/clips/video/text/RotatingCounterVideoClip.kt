package tech.softwarekitchen.moviekt.clips.video.text

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.basic.ContainerVideoClip
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage

data class RotatingCounterVideoClipConfiguration(
    val color: Color = Color.WHITE
)

class RotatingCounterVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: RotatingCounterVideoClipConfiguration
): ContainerVideoClip(id, size, position, visible) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass.name)
    private val tc1 = TextVideoClip(
        "_",
        size,
        Vector2i(0,0),
        true,
        StaticTextVideoClipConfiguration("0", color = configuration.color)
    )
    private val tc2 = TextVideoClip(
        "_",
        size,
        Vector2i(0,0),
        true,
        StaticTextVideoClipConfiguration("0", color = configuration.color)
    )

    companion object {
        val PropertyKey_Value = "Value"
    }

    private val propertyValue = VideoClipProperty<Double>(PropertyKey_Value, 0.0, this::markDirty)

    init{
        registerProperty(propertyValue)

        addChild(tc1, tc2)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val vLower = Math.floor(propertyValue.v).toInt()
        val vUpper = vLower + 1
        tc1.set(TextVideoClip.PropertyKey_Text, vLower.toString())
        tc2.set(TextVideoClip.PropertyKey_Text, vUpper.toString())
        val height = getSize().y

        val pixOff = (height * (propertyValue.v - vLower)).toInt()
        tc1.set(PropertyKey_Position, Vector2i(0, -pixOff))
        tc2.set(PropertyKey_Position, Vector2i(0, height - pixOff))

        super.renderContent(img, t)
    }
}