package tech.softwarekitchen.moviekt.clips.video.table

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.image.BufferedImage

class DataTableVideoClipConfiguration(
    val fontSize: Int = 24
)

class DataTableVideoClip(
    size: Vector2i, tOffset: Float,
    private val data: Array<Array<String>>,
    private val configuration: DataTableVideoClipConfiguration = DataTableVideoClipConfiguration(),
    visibilityDuration: Float? = null,
): VideoClip(size, tOffset, visibilityDuration) {
    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float): BufferedImage {
        val img = generateEmptyImage()

        val numRows = data.size
        val numColumns = data.maxOf{it.size}

        val spacePerColumn = size.x.toDouble() / numColumns
        val spacePerRow = size.y.toDouble() / numRows

        val graphics = img.createGraphics()
        (1 until numColumns).forEach{
            val x = (it * spacePerColumn).toInt()
            graphics.drawLine(x, 0, x, size.y)
        }
        (1 until numRows).forEach{
            val y = (it * spacePerRow).toInt()
            graphics.drawLine(0,y,size.x,y)
        }

        graphics.font = graphics.font.deriveFont(configuration.fontSize.toFloat())
        for(y in 0 until data.size){
            for(x in 0 until data[y].size){
                val centerX = (spacePerColumn * (0.5 + x)).toInt()
                val centerY = (spacePerRow * (0.5 + y)).toInt()
                val fontRect = graphics.font.getStringBounds(data[y][x], graphics.fontRenderContext)
                graphics.drawString(data[y][x], centerX - (fontRect.width / 2).toInt(), centerY + (fontRect.height / 2).toInt())
            }
        }

        return img
    }
}
