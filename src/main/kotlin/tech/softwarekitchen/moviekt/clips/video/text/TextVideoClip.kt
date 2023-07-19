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

enum class TextAnchor{
    Left, Center
}

class StaticTextVideoClipConfiguration(
    val text: String,
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null,
    val anchor: TextAnchor = TextAnchor.Left
)
class TextVideoClip (
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: StaticTextVideoClipConfiguration
) : VideoClip(id, size, position, visible) {
    private val font: Font?

    companion object{
        val PropertyKey_Text = "Text"
    }

    private val textProperty = VideoClipProperty(PropertyKey_Text, configuration.text, this::markDirty)

    init{
        font = configuration.ttFont?.let{
            Font.createFont(Font.TRUETYPE_FONT, it)
        }

        registerProperty(textProperty)
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
        val font = (font ?: graphics.font).deriveFont(configuration.fontSize.toFloat())
        val bounds = font.getStringBounds(textProperty.v,graphics.fontRenderContext)
        val topleft = when(configuration.anchor){
            TextAnchor.Center -> curSize.scale(0.5).plus(Vector2i(- bounds.width.toInt() / 2,- bounds.height.toInt() / 2))
            TextAnchor.Left -> Vector2i( 0, curSize.y / 2 - bounds.height.toInt() / 2)
        }
        val mapped = Vector2i(topleft.x - bounds.x.toInt() / 2, topleft.y - bounds.y.toInt())

        graphics.font = font
        graphics.color = configuration.color

        graphics.drawString(textProperty.v,mapped.x, mapped.y)
    }
}

fun String.getTextSize(f: Font): Rectangle2D{
    return f.getStringBounds(this, FontRenderContext(AffineTransform(), true, true))
}
