package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.util.Padding
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage

enum class DiagramAxisLegendMode{
    None, AxisOnly, Full
}
enum class DiagramAxisMode{
    Linear, Logarithmic
}
data class DiagramAxisConfiguration(
    val legendMode: DiagramAxisLegendMode = DiagramAxisLegendMode.AxisOnly,
    val min: Double? = null,
    val max: Double? = null,
    val unit: String? = null,
    val mode: DiagramAxisMode? = DiagramAxisMode.Linear,
    val title: String? = null
)

abstract class DiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val yAxis: DiagramAxisConfiguration,
    private val xAxis: DiagramAxisConfiguration,
    volatile: Boolean = false
): VideoClip(id, size,position, visible, volatile) {
    abstract fun generateDataDisplay(size: Vector2i): BufferedImage
    data class LegendEntry(val pos: Int, val legend: String)
    abstract fun getYLegendEntries(dataScreenHeight: Int): List<LegendEntry>
    abstract fun getXLegendEntries(dataScreenWidth: Int): List<LegendEntry>

    override fun renderContent(img: BufferedImage) {
        val bottomPadding = when(xAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Full -> 30
        } + when(xAxis.title){
            null -> 0
            else -> 30
        }
        val leftPadding = when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Full -> 70
        } + when(yAxis.title){
            null -> 0
            else -> 35
        }
        val padding = Padding(leftPadding,0,0,bottomPadding)

        val curSize = Vector2i(img.width, img.height)
        val dataDisplaySize = Vector2i(curSize.x - padding.left - padding.right, curSize.y - padding.top - padding.bottom)
        val targetGraph = img.createGraphics()

        val dataImage = generateDataDisplay(dataDisplaySize)
        targetGraph.drawImage(dataImage, padding.left, padding.top, null)

        drawYAxis(padding, targetGraph, dataDisplaySize, curSize)
        drawXAxis(padding, targetGraph, dataDisplaySize, curSize)
    }

    private fun drawYAxis(padding: Padding, graphics: Graphics2D, dataDisplaySize: Vector2i, totSize: Vector2i){
        val yShift = when(yAxis.title){
            null -> 0
            else -> 20
        }
        graphics.color = Color.WHITE
        when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> {}
            DiagramAxisLegendMode.AxisOnly -> {
                graphics.fillRect(padding.left-4,padding.top,3,dataDisplaySize.y)
            }
            DiagramAxisLegendMode.Full -> {
                val yAxisEntries = getYLegendEntries(dataDisplaySize.y)

                for(item in yAxisEntries){
                    graphics.fillRect(padding.left-7,padding.top + item.pos-2,7,5)
                    graphics.drawString(item.legend,yShift+2,padding.top + item.pos+8)
                }
                graphics.fillRect(padding.left-4,padding.top,3,dataDisplaySize.y)
            }
        }
        yAxis.title?.let{
            graphics.font = graphics.font.deriveFont(18f)
            val rect = graphics.font.getStringBounds(it, graphics.fontRenderContext)
            val width = rect.width
            val centerX = 10
            val centerY = (totSize.y - padding.bottom) / 2
            val forRestore = graphics.transform
            graphics.transform = AffineTransform.getRotateInstance(-Math.PI / 2, centerX.toDouble(), centerY.toDouble())
            graphics.drawString(it, (centerX - width / 2).toInt(), centerY + 4)
            graphics.transform = forRestore
        }

    }

    private fun drawXAxis(padding: Padding, graphics: Graphics2D, dataDisplaySize: Vector2i, totSize: Vector2i){
        graphics.color = Color.WHITE
        when(xAxis.legendMode){
            DiagramAxisLegendMode.None -> {}
            DiagramAxisLegendMode.AxisOnly -> {
                graphics.fillRect(padding.left,totSize.y - padding.bottom,dataDisplaySize.x-padding.right,3)
            }
            DiagramAxisLegendMode.Full -> {
                val xAxisEntries = getXLegendEntries(dataDisplaySize.x)
                for(item in xAxisEntries){
                    graphics.fillRect(padding.left+item.pos-2,totSize.y - padding.bottom,5,7)
                    graphics.drawString(item.legend,padding.left + item.pos-20,totSize.y - padding.bottom + 20)
                }
                graphics.fillRect(padding.left,totSize.y - padding.bottom,dataDisplaySize.x,3)
            }
        }

        xAxis.title?.let{
            graphics.font = graphics.font.deriveFont(18f)
            val rect = graphics.font.getStringBounds(it, graphics.fontRenderContext)
            graphics.drawString(it, (padding.left + (totSize.x - padding.left - rect.width) / 2).toInt(), totSize.y - 16 )
        }
    }
}
