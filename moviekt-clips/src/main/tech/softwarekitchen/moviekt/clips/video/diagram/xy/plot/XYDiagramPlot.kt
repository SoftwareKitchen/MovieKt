package tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot

import java.awt.image.BufferedImage

enum class XYDiagramReferenceAxis{
    Left, Right
}

interface XYDiagramPlot {
    val referenceAxis: XYDiagramReferenceAxis
    fun getData(): List<Pair<Double, Double>>
    fun plot(target: BufferedImage, xMapper: (Double) -> Int, yMapper: (Double) -> Int)
}
