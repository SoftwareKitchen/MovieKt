package tech.softwarekitchen.moviekt.clips.video.table

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.core.video.VideoClip
import java.awt.image.BufferedImage

class DataTableVideoClipConfiguration(
    val fontSize: Int = 24
)

class DataTableVideoClip(
    id: String, size: Vector2i,position: Vector2i, visible: Boolean,
    data: Array<Array<String>>,
    private val configuration: DataTableVideoClipConfiguration = DataTableVideoClipConfiguration()
): VideoClip(id, size, position, visible) {
    companion object{
        val PropertyKey_Data = "Data"
    }
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val dataProperty = VideoClipProperty(PropertyKey_Data, data,this::markDirty)

    init{
        registerProperty(dataProperty)
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val curSize = Vector2i(img.width, img.height)
        val data = dataProperty.v

        val numRows = data.size
        val numColumns = data.maxOf{it.size}

        val spacePerColumn = curSize.x.toDouble() / numColumns
        val spacePerRow = curSize.y.toDouble() / numRows

        val graphics = img.createGraphics()
        (1 until numColumns).forEach{
            val x = (it * spacePerColumn).toInt()
            graphics.drawLine(x, 0, x, curSize.y)
        }
        (1 until numRows).forEach{
            val y = (it * spacePerRow).toInt()
            graphics.drawLine(0,y,curSize.x,y)
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
    }
}
