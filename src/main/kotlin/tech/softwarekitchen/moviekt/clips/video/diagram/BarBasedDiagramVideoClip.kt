package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicDiagramBackgroundGrid
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramColorConfiguration

class BarBasedDiagramConfiguration(
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val grid: DynamicDiagramBackgroundGrid = DynamicDiagramBackgroundGrid.None,
    override val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration(),
    barWidth: Double = 1.0
): XYDiagramConfiguration

abstract class BarBasedDiagramVideoClip(
    size: SizeProvider,
    tOffset: Float, visibilityDuration: Float? = null,
    private val configuration: XYDiagramConfiguration
): XYDiagramVideoClip(
    size, tOffset, visibilityDuration = visibilityDuration, configuration = configuration
) {
    protected fun getYScreenMapper(dataScreenSize: Vector2i): (Double) -> Int{
        val dataBounds = getDataBounds()
        val totalDeltaExpY = when(configuration.yAxis.mode){
            DiagramAxisMode.Logarithmic -> Math.log10(dataBounds.ymax / dataBounds.ymin)
            else -> 0.0
        }
        val yScale: (Double) -> Int = if(configuration.yAxis.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.ymin)
                (dataScreenSize.y * (1 - deltaExp / totalDeltaExpY)).toInt()
            }
        }else{
            { (dataScreenSize.y * (1 - (it - dataBounds.ymin) / (dataBounds.ymax - dataBounds.ymin))).toInt() }
        }
        return yScale
    }

}
