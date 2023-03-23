package tech.softwarekitchen.moviekt.clips.video.diagram.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.PointBasedDiagramVideoClip
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramVideoClip
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage

enum class DynamicPointDiagramMarker{
    X, Plus
}

data class XYDataPoint(val x: Double, val y: Double)

data class DynamicPointDiagramVideoClipConfiguration(
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val grid: DynamicDiagramBackgroundGrid = DynamicDiagramBackgroundGrid.None,
    override val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
): XYDiagramConfiguration
class DynamicPointDiagramVideoClip(
    size: SizeProvider,
    tOffset: Float,
    private val dataSets: Map<DynamicPointDiagramMarker, () -> List<XYDataPoint>>,
    configuration: DynamicPointDiagramVideoClipConfiguration = DynamicPointDiagramVideoClipConfiguration(),
    visibilityDuration: Float? = null
): PointBasedDiagramVideoClip(
    size, tOffset, visibilityDuration,
    configuration = configuration,
) {
    override fun generateDataDisplay(
        size: Vector2i,
        frameNo: Int,
        nFrames: Int,
        tTotal: Float
    ): BufferedImage {
        val img = BufferedImage(size.x,size.y,BufferedImage.TYPE_INT_ARGB)
        drawBackgroundGrid(img, size)

        val graphics = img.createGraphics()
        val (xScale, yScale) = getScreenMapper(size)


        graphics.color = Color.WHITE

        for(dataLine in dataSets){
            val data = dataLine.value()
            val painter = getDataPointRenderer(dataLine.key)
            data.forEach {
                val x = xScale(it.x)
                val y = yScale(it.y)
                painter(graphics,x,y)
            }
        }

        return img
    }

    private fun getDataPointRenderer(type: DynamicPointDiagramMarker): (Graphics2D, Int, Int) -> Unit{
        return when(type){
            DynamicPointDiagramMarker.X -> {
                    graphics, x, y ->
                graphics.drawLine(x-5,y-5,x+5,y+5)
                graphics.drawLine(x-5,y+5,x+5,y-5)
            }
            DynamicPointDiagramMarker.Plus -> {
                graphics, x, y ->
                graphics.drawLine(x-5,y,x+5,y)
                graphics.drawLine(x,y-5,x,y+5)
            }
        }
    }

    override fun getData(): List<Pair<Double, Double>> {
        return dataSets.values.map{it()}.flatten().map{Pair(it.x, it.y)}
    }
}
