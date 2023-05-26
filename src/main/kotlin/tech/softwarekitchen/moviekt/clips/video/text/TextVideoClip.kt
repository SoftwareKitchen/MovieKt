package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.Font
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File

class StaticTextVideoClipConfiguration(
    val text: String,
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null
)
class TextVideoClip (
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: StaticTextVideoClipConfiguration
) : VideoClip(id, size, position, visible) {
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

    override fun renderContent(img: BufferedImage) {
        val curSize = Vector2i(img.width, img.height)

        val graphics = img.createGraphics()
        graphics.font = (font ?: graphics.font).deriveFont(configuration.fontSize.toFloat())
        graphics.color = Color(0,0,0,0)
        graphics.fillRect(0,0,curSize.x,curSize.y)
        graphics.color = configuration.color
        graphics.drawString(configuration.text,0,2*curSize.y/3)
    }
}

fun String.getTextSize(f: Font): Rectangle2D{
    return f.getStringBounds(this, FontRenderContext(AffineTransform(), true, true))
}
