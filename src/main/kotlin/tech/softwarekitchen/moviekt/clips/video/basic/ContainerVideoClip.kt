package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.exception.ChainedClipRequiresVisibilityDurationException
import tech.softwarekitchen.moviekt.exception.ClipSizeMismatchException
import java.awt.image.BufferedImage

fun VideoClip.chain(other: VideoClip): ContainerVideoClip{
    if(this.size.x != other.size.x || this.size.y != other.size.y){
        throw ClipSizeMismatchException()
    }
    val c1Length = this.visibilityDuration ?: throw ChainedClipRequiresVisibilityDurationException()
    val totalLength = when(val c2Length = other.visibilityDuration){
        null -> null
        else -> c1Length + c2Length
    }

    val c2AppearWrapper = ContainerVideoClip(other.size, c1Length,null) //Visibility duration can be set to 0 since it is limited by outer container visibilityDuration
    c2AppearWrapper.addChild(other, Vector2i(0,0))

    val container = ContainerVideoClip(this.size,0f,totalLength)
    container.addChild(this,Vector2i(0,0))
    container.addChild(c2AppearWrapper, Vector2i(0,0))
    return container
}

class ContainerVideoClip(
    size: Vector2i,
    tOffset: Float,
    visibilityDuration: Float?
): VideoClip(
    size, tOffset, visibilityDuration
) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        return img
    }
}
