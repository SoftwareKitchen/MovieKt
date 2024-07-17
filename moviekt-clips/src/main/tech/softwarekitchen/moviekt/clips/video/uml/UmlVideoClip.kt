package tech.softwarekitchen.moviekt.clips.video.uml

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.util.getDrawSize
import tech.softwarekitchen.moviekt.core.util.Padding
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import kotlin.math.max

class UmlField(val name: String, val type: String)
class UmlClass(val name: String, val fields: List<UmlField>)
class UmlVideoClipConfiguration(
    val clazz: UmlClass
)

class UmlVideoClip(
    id: String, size: Vector2i, position: Vector2i, visible: Boolean, private val configuration: UmlVideoClipConfiguration
): VideoClip(id, size, position, visible) {
    private val innerPadding = Padding(5,5,5,5)
    private val borderWidth = 3

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val graphics = img.createGraphics()

        val titleRawSize = configuration.clazz.name.getDrawSize(graphics)
        val titleBlockSize = Vector2i(
            innerPadding.left + innerPadding.right + titleRawSize.width.toInt(),
            innerPadding.top + innerPadding.bottom + titleRawSize.height.toInt()
        )
        val fieldContents = configuration.clazz.fields.map{"${it.name}: ${it.type}"}
        val fieldSizes = fieldContents.map{it.getDrawSize(graphics)}
        val fieldBlockSize = Vector2i(
            innerPadding.left + innerPadding.right + fieldSizes.maxOf{ it.width }.toInt(),
            innerPadding.top + innerPadding.bottom + fieldSizes.sumOf{it.height + 3}.toInt()
        )

        val maxBlockWidth = max(titleBlockSize.x, fieldBlockSize.x)

        val totalSize = Vector2i(
            2 * borderWidth + maxBlockWidth,
            3 * borderWidth + titleBlockSize.y + fieldBlockSize.y
        )

        val offsetLeft = (getSize() - totalSize).scale(0.5)
        graphics.color = Color(64,64,64)
        graphics.fillRect(offsetLeft.x, offsetLeft.y, totalSize.x, totalSize.y)
        graphics.color = Color(192,192,192)
        graphics.fillRect(offsetLeft.x + borderWidth, offsetLeft.y + borderWidth, maxBlockWidth, titleBlockSize.y)
        val fieldOffY = 2 * borderWidth + titleBlockSize.y
        graphics.fillRect(offsetLeft.x + borderWidth, offsetLeft.y + fieldOffY, maxBlockWidth, fieldBlockSize.y)

        graphics.color = Color.BLACK
        val centerX = offsetLeft.x + borderWidth + ((totalSize.x - titleRawSize.width) / 2 - titleRawSize.x).toInt()
        val centerY = offsetLeft.y + borderWidth + ((titleBlockSize.y - titleRawSize.height) / 2 - titleRawSize.y).toInt()
        val fontSave = graphics.font
        graphics.font = graphics.font.deriveFont(Font.BOLD)
        graphics.drawString(configuration.clazz.name,centerX, centerY)
        graphics.font = fontSave

        val fieldX = offsetLeft.x + borderWidth + innerPadding.left
        val fieldBaseY = offsetLeft.y + borderWidth * 2 + titleBlockSize.y + innerPadding.top

        var currentFieldY = fieldBaseY
        fieldContents.forEach{
            val bounds = it.getDrawSize(graphics)
            val effX = fieldX - bounds.x.toInt()
            val effY = currentFieldY - bounds.y.toInt()

            graphics.drawString(it, effX, effY)
            currentFieldY += 3 + bounds.height.toInt()
        }
    }
}
