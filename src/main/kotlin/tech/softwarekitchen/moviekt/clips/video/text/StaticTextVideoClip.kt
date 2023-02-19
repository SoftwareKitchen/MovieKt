package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB

class StaticTextVideoClipConfiguration(
    val fontSize: Int = 24,
    val color: Color = Color.BLACK
)
class StaticTextVideoClip (size: SizeProvider, private val text: String, private val configuration: StaticTextVideoClipConfiguration = StaticTextVideoClipConfiguration(), tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size, tOffset, visibilityDuration) {

    override fun renderContent(frameNo: Int, nFrames: Int, t: Float): BufferedImage {
        val curSize = size(frameNo, nFrames, t)
        val img = BufferedImage(curSize.x,curSize.y,TYPE_INT_ARGB)

        val graphics = img.createGraphics()
        graphics.font = graphics.font.deriveFont(configuration.fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,curSize.x,curSize.y)
        graphics.color = configuration.color
        graphics.drawString(text,0,2*curSize.y/3)

        return cloneImage(img)
    }
}