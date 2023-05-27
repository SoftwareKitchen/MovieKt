package tech.softwarekitchen.moviekt.clips.video.ui

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

data class ProgressBarVideoClipConfiguration(val color: Color = Color.BLUE)

class ProgressBarVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: ProgressBarVideoClipConfiguration = ProgressBarVideoClipConfiguration()
): VideoClip(id, size, position, visible) {
    companion object{
        val PropertyKey_Progress = "Progress"
    }
    private val progressProperty = VideoClipProperty(PropertyKey_Progress,0.0, this::markDirty)

    init{
        registerProperty(progressProperty)
    }
    override fun renderContent(img: BufferedImage) {
        val graphics = img.createGraphics()
        graphics.color = configuration.color

        val wid = (img.width * progressProperty.v).roundToInt()
        graphics.fillRect(0,0,wid,img.height)
    }
}
