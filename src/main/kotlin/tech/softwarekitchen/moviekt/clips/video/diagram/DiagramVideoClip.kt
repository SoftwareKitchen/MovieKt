package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

data class Padding(val left: Int, val right: Int, val top: Int, val bottom: Int)

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
    val mode: DiagramAxisMode? = DiagramAxisMode.Linear
)

abstract class DiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val yAxis: DiagramAxisConfiguration,
    private val xAxis: DiagramAxisConfiguration
): VideoClip(id, size,position, visible) {
    abstract fun generateDataDisplay(size: Vector2i): BufferedImage
    data class LegendEntry(val pos: Int, val legend: String)
    abstract fun getYLegendEntries(dataScreenHeight: Int): List<LegendEntry>
    abstract fun getXLegendEntries(dataScreenWidth: Int): List<LegendEntry>

    override fun renderContent(img: BufferedImage) {
        val bottomPadding = when(xAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Full -> 30
        }
        val leftPadding = when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Full -> 70
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
        graphics.color = Color.WHITE
        when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> {}
            DiagramAxisLegendMode.AxisOnly -> {
                graphics.fillRect(0,padding.top,padding.left,dataDisplaySize.y)
            }
            DiagramAxisLegendMode.Full -> {
                val yAxisEntries = getYLegendEntries(dataDisplaySize.y)

                for(item in yAxisEntries){
                    graphics.fillRect(padding.left-7,padding.top + item.pos-2,7,5)
                    graphics.drawString(item.legend,2,padding.top + item.pos+8)
                }
                graphics.fillRect(padding.left-4,padding.top,3,dataDisplaySize.y)
            }
        }
    }

    private fun drawXAxis(padding: Padding, graphics: Graphics2D, dataDisplaySize: Vector2i, totSize: Vector2i){
        graphics.color = Color.WHITE
        when(xAxis.legendMode){
            DiagramAxisLegendMode.None -> {}
            DiagramAxisLegendMode.AxisOnly -> {
                graphics.fillRect(padding.left,totSize.y - padding.bottom,dataDisplaySize.x-padding.right,totSize.y)
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
    }
}
