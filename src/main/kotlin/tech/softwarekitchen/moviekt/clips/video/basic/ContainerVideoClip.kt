package tech.softwarekitchen.moviekt.clips.video.basic

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage

class ContainerVideoClip(
    size: Vector2i,
    tOffset: Float,
    visibilityDuration: Float?
): VideoClip(
    size, tOffset, visibilityDuration
) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        val img = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        return img
    }
}
