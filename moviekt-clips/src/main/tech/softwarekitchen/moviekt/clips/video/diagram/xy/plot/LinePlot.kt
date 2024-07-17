package tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Polygon
import java.awt.image.BufferedImage

class LinePlot(
    val plotData: List<Pair<Double, Double>>,
    val lineColor: Color,
    val underGraphColor: Color?,
    override val referenceAxis: XYDiagramReferenceAxis = XYDiagramReferenceAxis.Left
): XYDiagramPlot {

    override fun getData(): List<Pair<Double, Double>> = plotData

    override fun plot(target: BufferedImage, xMapper: (Double) -> Int, yMapper: (Double) -> Int) {
        val graphics = target.createGraphics()
        val mappedPoints = plotData.map{ Pair(xMapper(it.first), yMapper(it.second)) }
        val yZero = yMapper(0.0)

        underGraphColor?.let{
            graphics.color = it
            graphics.stroke = BasicStroke(1f)
            (0 until mappedPoints.size - 1).forEach{
                val shape = Polygon()
                shape.addPoint(mappedPoints[it].first, yZero)
                shape.addPoint(mappedPoints[it+1].first,yZero)
                shape.addPoint(mappedPoints[it+1].first,mappedPoints[it+1].second)
                shape.addPoint(mappedPoints[it].first, mappedPoints[it].second)
                graphics.fill(shape)
            }
        }

        graphics.setStroke(BasicStroke(3f))
        graphics.color = lineColor
        (0 until mappedPoints.size - 1).forEach{
            val shape = Polygon()
            graphics.drawLine(mappedPoints[it].first, mappedPoints[it].second, mappedPoints[it+1].first, mappedPoints[it+1].second)
        }

    }
}
