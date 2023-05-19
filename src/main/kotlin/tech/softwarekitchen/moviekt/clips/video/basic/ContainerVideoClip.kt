package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.exception.ChainedClipRequiresVisibilityDurationException
import tech.softwarekitchen.moviekt.exception.ClipSizeMismatchException
import java.awt.BasicStroke
import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

fun VideoClip.chain(other: VideoClip): ContainerVideoClip{
    if(this.size(0,0,0f).x != other.size(0,0,0f).x || this.size(0,0,0f).y != other.size(0,0,0f).y){
        throw ClipSizeMismatchException()
    }
    val c1Length = this.visibilityDuration ?: throw ChainedClipRequiresVisibilityDurationException()
    val totalLength = when(val c2Length = other.visibilityDuration){
        null -> null
        else -> c1Length + c2Length
    }

    val c2AppearWrapper = ContainerVideoClip(other.size, c1Length,null) //Visibility duration can be set to null since it is limited by outer container visibilityDuration
    c2AppearWrapper.addChild(other, Vector2i(0,0))

    val container = ContainerVideoClip(this.size,0f,totalLength)
    container.addChild(this,Vector2i(0,0))
    container.addChild(c2AppearWrapper, Vector2i(0,0))
    return container
}

data class ContainerBorderConfiguration(
    val width: Int = 0,
    val color: Color = Color.WHITE
)
data class ContainerVideoClipConfiguration(
    val border: ContainerBorderConfiguration = ContainerBorderConfiguration()
)

open class ContainerVideoClip(
    size: SizeProvider,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
    private val configuration: ContainerVideoClipConfiguration = ContainerVideoClipConfiguration()
): VideoClip(
    size, tOffset, visibilityDuration
) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = BufferedImage(size(frameNo, nFrames, tTotal).x,size(frameNo, nFrames, tTotal).y,BufferedImage.TYPE_INT_ARGB)

        if(configuration.border.width > 0){
            val size = getSize(frameNo, nFrames, tTotal)
            val graphics = img.createGraphics()
            val borderWidth = configuration.border.width.toFloat()
            graphics.stroke = BasicStroke(borderWidth)
            graphics.color = configuration.border.color
            graphics.drawRect((0 + borderWidth / 2).roundToInt(),(0 + borderWidth / 2).roundToInt(), (size.x - 2 * borderWidth).roundToInt(), (size.y - 2 * borderWidth).roundToInt())
        }

        return img
    }
}
