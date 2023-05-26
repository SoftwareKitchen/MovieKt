package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
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
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: XYDiagramConfiguration
): XYDiagramVideoClip(
    id, size, position, visible, configuration
) {
    protected fun getYScreenMapper(dataScreenSize: Vector2i, addOne: Boolean = false): (Double) -> Int{
        val dataBounds = getDataBounds()
        val yAdd = when(addOne){
            true -> 1.0
            else -> 0.0
        }

        val totalDeltaExpY = when(configuration.yAxis.mode){
            DiagramAxisMode.Logarithmic -> Math.log10((yAdd+dataBounds.ymax) / dataBounds.ymin)
            else -> 0.0
        }
        val yScale: (Double) -> Int = if(configuration.yAxis.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.ymin)
                (dataScreenSize.y * (1 - deltaExp / totalDeltaExpY)).toInt()
            }
        }else{
            { (dataScreenSize.y * (1 - (it - dataBounds.ymin) / (yAdd + dataBounds.ymax - dataBounds.ymin))).toInt() }
        }
        return yScale
    }

    //For vertical diagrams
    protected fun getXScreenMapper(dataScreenSize: Vector2i): (Double) -> Int{
        val dataBounds = getDataBounds()
        val totalDeltaExpX = when(configuration.xAxis.mode){
            DiagramAxisMode.Logarithmic -> Math.log10(dataBounds.xmax / dataBounds.xmin)
            else -> 0.0
        }
        val xScale: (Double) -> Int = if(configuration.xAxis.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.xmin)
                (dataScreenSize.x * (deltaExp / totalDeltaExpX)).toInt()
            }
        }else{
            { (dataScreenSize.x * (it - dataBounds.xmin) / (dataBounds.xmax - dataBounds.xmin)).toInt() }
        }
        return xScale
    }
}
