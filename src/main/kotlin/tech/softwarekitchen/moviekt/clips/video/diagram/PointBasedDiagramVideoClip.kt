package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramBackgroundGrid
import java.awt.Color
import java.awt.image.BufferedImage

abstract class PointBasedDiagramVideoClip(
    base: Vector2i, size: Vector2i,
    tOffset: Float, visibilityDuration: Float? = null,
    yAxis: DiagramAxisConfiguration, xAxis: DiagramAxisConfiguration,
    private val configuration: XYDiagramConfiguration
): XYDiagramVideoClip(
    base, size, tOffset, visibilityDuration = visibilityDuration, yAxis = yAxis, xAxis = xAxis, configuration = configuration
) {
    protected fun getScreenMapper(dataScreenSize: Vector2i): Pair<(Double) -> Int, (Double) -> Int>{
        val dataBounds = getDataBounds()
        val totalDeltaExpX = when(configuration.xAxis.mode){
            DiagramAxisMode.Logarithmic -> Math.log10(dataBounds.xmax / dataBounds.xmin)
            else -> 0.0
        }
        val totalDeltaExpY = when(configuration.yAxis.mode){
            DiagramAxisMode.Logarithmic -> Math.log10(dataBounds.ymax / dataBounds.ymin)
            else -> 0.0
        }
        val xScale: (Double) -> Int = if(configuration.xAxis.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.xmin)
                (dataScreenSize.x * deltaExp / totalDeltaExpX).toInt()
            }
        }else{
            { (dataScreenSize.x * (it - dataBounds.xmin) / (dataBounds.xmax - dataBounds.xmin)).toInt() }
        }
        val yScale: (Double) -> Int = if(configuration.yAxis.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.ymin)
                (dataScreenSize.y * (1 - deltaExp / totalDeltaExpY)).toInt()
            }
        }else{
            { (dataScreenSize.y * (1 - (it - dataBounds.ymin) / (dataBounds.ymax - dataBounds.ymin))).toInt() }
        }
        return Pair(xScale, yScale)
    }

    protected fun drawBackgroundGrid(image: BufferedImage){
        val graphics = image.createGraphics()
        graphics.color = Color(255,255,255,128)
        when(configuration.grid){
            DynamicLineDiagramBackgroundGrid.None -> {}
            DynamicLineDiagramBackgroundGrid.X -> {
                for(item in getXLegendEntries(image.width)){
                    graphics.fillRect(item.pos-1,0,3 , size.y)
                }
            }
            DynamicLineDiagramBackgroundGrid.Y -> {
                for(item in getYLegendEntries(image.height)){
                    graphics.fillRect(0,item.pos-1,size.x , 3)
                }
            }
        }
    }
}
