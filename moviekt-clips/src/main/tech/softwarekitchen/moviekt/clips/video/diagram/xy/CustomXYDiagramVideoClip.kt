package tech.softwarekitchen.moviekt.clips.video.diagram.xy

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramVideoClip
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicDiagramBackgroundGrid
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramColorConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot.XYDiagramPlot
import java.awt.image.BufferedImage

class CustomXYDiagramVideoClipConfiguration(
    val plots: List<XYDiagramPlot>,
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val grid: DynamicDiagramBackgroundGrid = DynamicDiagramBackgroundGrid.None,
    override val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
): XYDiagramConfiguration

class CustomXYDiagramVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    configuration: CustomXYDiagramVideoClipConfiguration,
    volatile: Boolean = true
): XYDiagramVideoClip(id, size, position, visible, configuration, volatile = volatile) {
    companion object{
        val PropertyKey_Plots = "CustomXY_Plots"
    }

    val plotsProperty = VideoClipProperty(PropertyKey_Plots, configuration.plots, this::markDirty)

    init{
        registerProperty(plotsProperty)
    }

    override fun getData(): List<Pair<Double, Double>> = plotsProperty.v.map{it.getData()}.flatten()

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB)
        val (xMapper, yMapper) = getScreenMapper(size)
        plotsProperty.v.forEach{
            it.plot(image, xMapper, yMapper)
        }

        return image
    }
}