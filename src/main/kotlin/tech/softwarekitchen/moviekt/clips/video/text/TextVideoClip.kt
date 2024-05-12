package tech.softwarekitchen.moviekt.clips.video.text

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.theme.VideoTheme
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

    companion object{
        val PropertyKey_Text = "Text"
    }
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val textProperty = VideoClipProperty(PropertyKey_Text, configuration.text, this::markDirty)
    private val fontColorProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_FontColor, configuration.color, this::markDirty)
    private val fontSizeProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_FontSize, configuration.fontSize, this::markDirty)
    private val fontProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_Font, configuration.ttFont?.let(this::loadFont), this::markDirty){
        when{
            it is Font -> it
            it is File -> loadFont(it)
            else -> throw Exception()
        }
    }

    init{
        registerProperty(textProperty)
        registerProperty(fontProperty)
        registerProperty(fontColorProperty)
        registerProperty(fontSizeProperty)
    }

    private fun loadFont(file: File): Font{
        return Font.createFont(Font.TRUETYPE_FONT, file)
    }

    fun getTextSize(text: String): Rectangle2D {
        val f = fontProperty.v ?: run {
            val img = BufferedImage(100,100,TYPE_INT_ARGB)
            val graphics = img.createGraphics()
            graphics.font!!
        }

        return f.deriveFont(fontSizeProperty.v.toFloat()).getStringBounds(text, FontRenderContext(AffineTransform(), true, true))
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val curSize = Vector2i(img.width, img.height)

        val graphics = img.createGraphics()
        val font = (fontProperty.v ?: graphics.font).deriveFont(fontSizeProperty.v.toFloat())
        val bounds = font.getStringBounds(textProperty.v,graphics.fontRenderContext)
        val topleft = when(configuration.anchor){
            TextAnchor.Center -> curSize.scale(0.5).plus(Vector2i(- bounds.width.toInt() / 2,- bounds.height.toInt() / 2))
            TextAnchor.Left -> Vector2i( 0, curSize.y / 2 - bounds.height.toInt() / 2)
        }
        val mapped = Vector2i(topleft.x - bounds.x.toInt() / 2, topleft.y - bounds.y.toInt())

        graphics.font = font
        graphics.color = fontColorProperty.v

        graphics.drawString(textProperty.v,mapped.x, mapped.y)
    }
}

fun String.getTextSize(f: Font): Rectangle2D{
    return f.getStringBounds(this, FontRenderContext(AffineTransform(), true, true))
}

class DynamicTextVideoClipConfiguration(
    val callback: (VideoTimestamp) -> String,
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null,
    val anchor: TextAnchor = TextAnchor.Left
)
open class DynamicTextVideoClip (
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: DynamicTextVideoClipConfiguration
) : VideoClip(id, size, position, visible, volatile = true) {

    companion object{
        val PropertyKey_Text = "Text"
    }
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val fontColorProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_FontColor, configuration.color, this::markDirty)
    private val fontSizeProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_FontSize, configuration.fontSize, this::markDirty)
    private val fontProperty = VideoClipThemeProperty(VideoTheme.VTPropertyKey_Font, configuration.ttFont?.let(this::loadFont), this::markDirty){
        when{
            it is Font -> it
            it is File -> loadFont(it)
            else -> throw Exception()
        }
    }

    init{
        registerProperty(fontProperty)
        registerProperty(fontColorProperty)
        registerProperty(fontSizeProperty)
    }

    private fun loadFont(file: File): Font{
        return Font.createFont(Font.TRUETYPE_FONT, file)
    }

    fun getTextSize(text: String): Rectangle2D {
        val f = fontProperty.v ?: run {
            val img = BufferedImage(100,100,TYPE_INT_ARGB)
            val graphics = img.createGraphics()
            graphics.font!!
        }

        return f.deriveFont(fontSizeProperty.v.toFloat()).getStringBounds(text, FontRenderContext(AffineTransform(), true, true))
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val curSize = Vector2i(img.width, img.height)
        val text = configuration.callback(t)

        val graphics = img.createGraphics()
        val font = (fontProperty.v ?: graphics.font).deriveFont(fontSizeProperty.v.toFloat())
        val bounds = font.getStringBounds(text,graphics.fontRenderContext)
        val topleft = when(configuration.anchor){
            TextAnchor.Center -> curSize.scale(0.5).plus(Vector2i(- bounds.width.toInt() / 2,- bounds.height.toInt() / 2))
            TextAnchor.Left -> Vector2i( 0, curSize.y / 2 - bounds.height.toInt() / 2)
        }
        val mapped = Vector2i(topleft.x - bounds.x.toInt() / 2, topleft.y - bounds.y.toInt())

        graphics.font = font
        graphics.color = fontColorProperty.v

        graphics.drawString(text,mapped.x, mapped.y)
    }
}

class TextTimerVideoClipConfiguration(
    val fontSize: Int = 24,
    val color: Color = Color.BLACK,
    val ttFont: File? = null,
    val anchor: TextAnchor = TextAnchor.Left
){
    fun toDynammic(): DynamicTextVideoClipConfiguration{
        return DynamicTextVideoClipConfiguration(
            {
                val sTot = it.t.toInt()
                val min = (sTot / 60) % 60
                val sec = sTot % 60
                val h = sTot / 3600

                val sStr = "$sec".padStart(2, '0')
                if(h > 0){
                    val mStr = "$min".padStart(2, '0')
                    "$h:$mStr:$sStr"
                }else{
                    "$min:$sStr"

                }
            },
            fontSize, color, ttFont, anchor
        )
    }
}


class TextTimerVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    configuration: TextTimerVideoClipConfiguration
): DynamicTextVideoClip(
    id, size, position, visible, configuration.toDynammic()
){

}