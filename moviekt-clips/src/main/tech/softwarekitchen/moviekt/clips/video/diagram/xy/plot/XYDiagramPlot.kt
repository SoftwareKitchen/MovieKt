package tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot

import java.awt.image.BufferedImage

interface XYDiagramPlot {
    fun getData(): List<Pair<Double, Double>>
    fun plot(target: BufferedImage, xMapper: (Double) -> Int, yMapper: (Double) -> Int)
}
