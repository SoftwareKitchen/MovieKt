package tech.softwarekitchen.moviekt.clips.video.ui

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

enum class ProgressBarDirectionMode{
    HorizontalRTL,HorizontalLTR,VerticalTTB, VerticalBTT
}
data class ProgressBarVideoClipConfiguration(val color: Color = Color.BLUE, val mode: ProgressBarDirectionMode = ProgressBarDirectionMode.HorizontalRTL)

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
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val progressProperty = VideoClipProperty(PropertyKey_Progress,0.0, this::markDirty)

    init{
        registerProperty(progressProperty)
    }
    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()
        graphics.color = configuration.color

        when(configuration.mode){
            ProgressBarDirectionMode.HorizontalRTL -> {
                val wid = (img.width * progressProperty.v).roundToInt()
                graphics.fillRect(0,0,wid,img.height)
            }
            ProgressBarDirectionMode.HorizontalLTR -> {
                val wid = (img.width * progressProperty.v).roundToInt()
                graphics.fillRect(img.width-wid,0,wid,img.height)

            }
            ProgressBarDirectionMode.VerticalBTT -> {
                val hei = (img.height * progressProperty.v).roundToInt()
                graphics.fillRect(0,0,img.width,hei)
            }
            ProgressBarDirectionMode.VerticalTTB -> {
                val hei = (img.height * progressProperty.v).roundToInt()
                graphics.fillRect(0,img.height - hei,img.width,hei)
            }
        }
    }
}
