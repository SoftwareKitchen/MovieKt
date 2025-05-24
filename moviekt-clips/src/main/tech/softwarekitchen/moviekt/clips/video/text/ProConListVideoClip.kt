package tech.softwarekitchen.moviekt.clips.video.text

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.audio.basic.AudioContainerClip
import tech.softwarekitchen.moviekt.clips.video.basic.ColorVideoClip
import tech.softwarekitchen.moviekt.clips.video.basic.ColorVideoClipConfiguration
import tech.softwarekitchen.moviekt.clips.video.util.CLIP_ORIGIN
import tech.softwarekitchen.moviekt.clips.video.util.FULLHD
import tech.softwarekitchen.moviekt.core.Movie
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.theme.VideoTheme
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File

/**
 * Represents a single item in the pro/con list with text content and a level
 * indicating its positivity/negativity from -3 (very negative) to +3 (very positive)
 */
data class ProConItem(
    val text: String,
    val level: Int
) {
    init {
        require(level in -3..3) { "Level must be between -3 and +3" }
    }

    val isPositive: Boolean get() = level > 0
    val isNegative: Boolean get() = level < 0
    val isNeutral: Boolean get() = level == 0
}

/**
 * Configuration for the ProConListVideoClip
 */
class ProConListVideoClipConfiguration(
    val items: List<ProConItem>,
    val fontSize: Int = 20,
    val fontColor: Color = Color.WHITE,
    val ttFont: File? = null,
    val positiveColor: Color = Color(0, 200, 0), // Green for positive
    val negativeColor: Color = Color(200, 0, 0), // Red for negative
    val backgroundColor: Color = Color.BLACK,
    val itemSpacing: Int = 10
)

/**
 * A VideoClip that displays a list of pros and cons with visual indicators
 * based on their level (-3 to +3).
 */
class ProConListVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: ProConListVideoClipConfiguration
) : VideoClip(id, size, position, visible) {

    companion object {
        val PropertyKey_Items = "Items"
        val PropertyKey_PositiveColor = "PositiveColor"
        val PropertyKey_NegativeColor = "NegativeColor"
        val PropertyKey_ItemSpacing = "ItemSpacing"
    }

    private val logger: Logger = LoggerFactory.getLogger(ProConListVideoClip::class.java)
    
    // Properties for configuration
    protected val itemsProperty = VideoClipProperty(PropertyKey_Items, configuration.items, this::markDirty)
    protected val fontColorProperty = VideoClipProperty(VideoTheme.VTPropertyKey_FontColor, configuration.fontColor, this::markDirty)
    protected val fontSizeProperty = VideoClipProperty(VideoTheme.VTPropertyKey_FontSize, configuration.fontSize, this::markDirty)
    protected val fontProperty = VideoClipProperty(VideoTheme.VTPropertyKey_Font, configuration.ttFont?.let(this::loadFont), this::markDirty) {
        when {
            it is Font -> it
            it is File -> loadFont(it)
            else -> throw Exception("Unable to parse to font: $it")
        }
    }
    protected val backgroundColorProperty = VideoClipProperty(VideoTheme.VTPropertyKey_BackgroundColor, configuration.backgroundColor, this::markDirty)
    protected val positiveColorProperty = VideoClipProperty(PropertyKey_PositiveColor, configuration.positiveColor, this::markDirty)
    protected val negativeColorProperty = VideoClipProperty(PropertyKey_NegativeColor, configuration.negativeColor, this::markDirty)
    protected val itemSpacingProperty = VideoClipProperty(PropertyKey_ItemSpacing, configuration.itemSpacing, this::markDirty)

    private fun loadFont(file: File): Font{
        return Font.createFont(Font.TRUETYPE_FONT, file)
    }

    init {
        // Register all properties
        registerProperty(itemsProperty)
        registerProperty(fontProperty)
        registerProperty(fontColorProperty)
        registerProperty(fontSizeProperty)
        registerProperty(backgroundColorProperty)
        registerProperty(positiveColorProperty)
        registerProperty(negativeColorProperty)
        registerProperty(itemSpacingProperty)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()

        // Fill background
        graphics.color = backgroundColorProperty.v
        graphics.fillRect(0, 0, getSize().x, getSize().y)

        // Get the font to use
        val regularFont = (fontProperty.v ?: graphics.font).deriveFont(fontSizeProperty.v.toFloat())
        val boldFont = regularFont.deriveFont(Font.BOLD) // Create bold font for indicators
        
        // Sort items - most positive first, most negative last
        val sortedItems = itemsProperty.v.sortedByDescending { it.level }
        
        // Calculate item height based on font metrics
        graphics.font = regularFont
        val fontMetrics = graphics.fontMetrics
        val itemHeight = fontMetrics.height
        val padding = 10
        
        // Start position (centered horizontally, aligned top vertically with some padding)
        val startY = padding
        val startX = padding
        val indicatorWidth = 40 // Fixed width for the indicator section
        
        // Draw each item
        var currentY = startY
        
        sortedItems.forEach { item ->
            // Determine prefix symbols based on level
            val prefix = when {
                item.level > 0 -> "+" * item.level  // Repeat plus signs based on level
                item.level < 0 -> "-" * (-item.level) // Repeat minus signs based on absolute value of negative level
                else -> ""  // Neutral items get no prefix
            }
        
            if (prefix.isNotEmpty()) {
                // Draw prefix with bold font
                graphics.font = boldFont
                graphics.color = if (item.level > 0) positiveColorProperty.v else negativeColorProperty.v
                graphics.drawString(prefix, startX, currentY + itemHeight)
            }
        
            // Draw main text with regular font, always starting at the same x position
            graphics.font = regularFont
            graphics.color = fontColorProperty.v
            graphics.drawString(item.text, startX + indicatorWidth, currentY + itemHeight)
        
            // Move to next line position
            currentY += itemHeight + itemSpacingProperty.v
        }
    }
    
    // Helper function to repeat a string n times
    private operator fun String.times(n: Int): String = this.repeat(n)
}


fun main(){
    val root = ColorVideoClip("_", FULLHD, CLIP_ORIGIN, true, ColorVideoClipConfiguration(Color(40,40,40)))
    val contains = ProConListVideoClip("_", Vector2i(600,800), Vector2i(50,50), true,
        ProConListVideoClipConfiguration(
            listOf(
                ProConItem("Kekse", +3),
            ProConItem("Keine Kekse", -3),
                ProConItem("Aufräumen", -2)
        )
    ))
    root.addChild(contains)
    val video = Movie("Testy", 10, 60, root, AudioContainerClip(1))
    video.write()
}