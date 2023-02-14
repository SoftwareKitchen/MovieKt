package tech.softwarekitchen.moviekt.clips.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.Clip
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

data class Padding(val left: Int, val right: Int, val top: Int, val bottom: Int)

enum class DiagramAxisLegendMode{
    None, AxisOnly, Default
}
data class DiagramAxisConfiguration(
    val legendMode: DiagramAxisLegendMode = DiagramAxisLegendMode.Default,
    val min: Double? = null,
    val max: Double? = null,
    val unit: String? = null
)

abstract class DiagramClip(
    base: Vector2i,
    size: Vector2i,
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
    private val yAxis: DiagramAxisConfiguration,
    private val xAxis: DiagramAxisConfiguration
): Clip(base,size,tOffset,visibilityDuration) {
    private val padding: Padding
    private val dataDisplaySize: Vector2i
    init{
        val bottomPadding = when(xAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Default -> 30
        }
        val leftPadding = when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> 0
            DiagramAxisLegendMode.AxisOnly -> 3
            DiagramAxisLegendMode.Default -> 70
        }
        padding = Padding(leftPadding,0,0,bottomPadding)
        dataDisplaySize = Vector2i(size.x - padding.left - padding.right, size.y - padding.top - padding.bottom)
    }

    abstract fun generateDataDisplay(size: Vector2i, frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage
    data class YLegendEntry(val yPos: Int, val legend: String)
    abstract fun getYLegendEntries(dataScreenHeight: Int): List<YLegendEntry>


    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        val target = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        val targetGraph = target.createGraphics()

        val dataImage = generateDataDisplay(dataDisplaySize, frameNo, nFrames, tTotal, tInternal)
        targetGraph.drawImage(dataImage, padding.left, padding.top, null)

        drawYAxis(targetGraph)
        return target
    }

    private fun drawYAxis(graphics: Graphics2D){
        when(yAxis.legendMode){
            DiagramAxisLegendMode.None -> {}
            DiagramAxisLegendMode.AxisOnly -> {
                graphics.color = Color.WHITE
                graphics.fillRect(0,padding.top,padding.left,dataDisplaySize.y)
            }
            DiagramAxisLegendMode.Default -> {
                graphics.color = Color.WHITE

                val yAxisEntries = getYLegendEntries(dataDisplaySize.y)
                for(item in yAxisEntries){
                    graphics.fillRect(padding.left-7,padding.top + item.yPos-2,7,5)
                    graphics.drawString(item.legend,2,padding.top + item.yPos+8)
                }
                graphics.fillRect(padding.left-4,padding.top,3,dataDisplaySize.y)
            }
        }
    }
}
