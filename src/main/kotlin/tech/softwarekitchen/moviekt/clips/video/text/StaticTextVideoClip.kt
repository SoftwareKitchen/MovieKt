package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File

class StaticTextVideoClipConfiguration(
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null
)
class StaticTextVideoClip (size: SizeProvider, private val text: String, private val configuration: StaticTextVideoClipConfiguration = StaticTextVideoClipConfiguration(), tOffset: Float = 0f, visibilityDuration: Float? = null): VideoClip(size, tOffset, visibilityDuration) {
    private val font: Font?

    init{
        font = configuration.ttFont?.let{
            Font.createFont(Font.TRUETYPE_FONT, it)
        }
    }

    fun getTextSize(text: String): Rectangle2D {
        val f = font ?: run {
            val img = BufferedImage(100,100,TYPE_INT_ARGB)
            val graphics = img.createGraphics()
            graphics.font!!
        }

        return f.deriveFont(configuration.fontSize.toFloat()).getStringBounds(text, FontRenderContext(AffineTransform(), true, true))
    }

    override fun renderContent(frameNo: Int, nFrames: Int, t: Float): BufferedImage {
        val curSize = size(frameNo, nFrames, t)
        val img = BufferedImage(curSize.x,curSize.y,TYPE_INT_ARGB)

        val graphics = img.createGraphics()
        graphics.font = (font ?: graphics.font).deriveFont(configuration.fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,curSize.x,curSize.y)
        graphics.color = configuration.color
        graphics.drawString(text,0,2*curSize.y/3)

        return cloneImage(img)
    }
}