package tech.softwarekitchen.moviekt.clips.video.diagram.xy

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.DiagramAxisMode
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.XYDiagramVideoClip
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicDiagramBackgroundGrid
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramColorConfiguration
import tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot.XYDiagramPlot
import tech.softwarekitchen.moviekt.clips.video.diagram.xy.plot.XYDiagramReferenceAxis
import java.awt.image.BufferedImage

class CustomXYDiagramVideoClipConfiguration(
    val plots: List<XYDiagramPlot>,
    override val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    override val yAxis2: DiagramAxisConfiguration? = null,
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

    protected fun getY2ScreenMapper(dataScreenSize: Vector2i): (Double) -> Int{
        val plots = plotsProperty.v.filter{it.referenceAxis == XYDiagramReferenceAxis.Right}
        val dataBounds = DataBounds(
            xAxisConfigurationProperty.v.min ?: plots.minOfOrNull{ it.getData().minOfOrNull{it.first} ?: 0.0} ?: 0.0,
            yAxis2ConfigurationProperty.v?.min ?: plots.minOfOrNull{ it.getData().minOfOrNull{it.second} ?: 0.0} ?: 0.0,
            xAxisConfigurationProperty.v.max ?: plots.maxOfOrNull{ it.getData().maxOfOrNull{it.first} ?: 1.0} ?: 1.0,
            yAxis2ConfigurationProperty.v?.max ?: plots.maxOfOrNull{ it.getData().maxOfOrNull{it.second} ?: 1.0} ?: 1.0,
        )
        val totalDeltaExpY = when(yAxis2ConfigurationProperty.v!!.mode){
            DiagramAxisMode.Logarithmic -> Math.log10(dataBounds.ymax / dataBounds.ymin)
            else -> 0.0
        }
        val yScale: (Double) -> Int = if(yAxis2ConfigurationProperty.v!!.mode == DiagramAxisMode.Logarithmic){
            {
                val deltaExp = Math.log10(it / dataBounds.ymin)
                (dataScreenSize.y * (1 - deltaExp / totalDeltaExpY)).toInt()
            }
        }else{
            { (dataScreenSize.y * (1 - (it - dataBounds.ymin) / (dataBounds.ymax - dataBounds.ymin))).toInt() }
        }

        return yScale
    }
    override fun getY2LegendEntries(dataScreenHeight: Int): List<LegendEntry> {
        val plots = plotsProperty.v.filter{it.referenceAxis == XYDiagramReferenceAxis.Right}

        val dataBounds = if(plots.isEmpty() || plots.all{it.getData().isEmpty()}){
            DataBounds(0.0,0.0,1.0,1.0)
        }else {
            DataBounds(
                xAxisConfigurationProperty.v.min ?: plots.minOf { it.getData().minOf { it.first } },
                yAxis2ConfigurationProperty.v?.min ?: plots.minOf { it.getData().minOf { it.second } },
                xAxisConfigurationProperty.v.max ?: plots.maxOf { it.getData().maxOf { it.first } },
                yAxis2ConfigurationProperty.v?.max ?: plots.maxOf { it.getData().maxOf { it.second } }
            )
        }
        val min = dataBounds.ymin
        val max = dataBounds.ymax
        val unit = when(val v = yAxis2ConfigurationProperty.v?.unit){
            null -> ""
            else -> v
        }
        if(yAxis2ConfigurationProperty.v?.mode == DiagramAxisMode.Logarithmic){
            return generateLogarithmicBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
        }
        return generateLinearBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
    }

    override fun getYLegendEntries(dataScreenHeight: Int): List<LegendEntry> {
        val plots = plotsProperty.v.filter{it.referenceAxis == XYDiagramReferenceAxis.Left}

        val dataBounds = if(plots.isEmpty() || plots.all{it.getData().isEmpty()}){
            DataBounds(0.0,0.0,1.0,1.0)
        } else {
            DataBounds(
                xAxisConfigurationProperty.v.min ?: plots.minOf { it.getData().minOf { it.first } },
                yAxisConfigurationProperty.v.min ?: plots.minOf { it.getData().minOf { it.second } },
                xAxisConfigurationProperty.v.max ?: plots.minOf { it.getData().maxOf { it.first } },
                yAxisConfigurationProperty.v.max ?: plots.minOf { it.getData().maxOf { it.second } }
            )
        }
        val min = dataBounds.ymin
        val max = dataBounds.ymax
        val unit = when(val v = yAxisConfigurationProperty.v.unit){
            null -> ""
            else -> v
        }
        if(yAxisConfigurationProperty.v.mode == DiagramAxisMode.Logarithmic){
            return generateLogarithmicBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
        }
        return generateLinearBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
    }

    override fun generateDataDisplay(size: Vector2i): BufferedImage {
        val image = BufferedImage(size.x, size.y, BufferedImage.TYPE_INT_ARGB)
        val (xMapper, yMapper) = getScreenMapper(size)
        val yMapper2 = getY2ScreenMapper(size)
        plotsProperty.v.forEach{
            if(it.referenceAxis == XYDiagramReferenceAxis.Left){
                it.plot(image, xMapper, yMapper)
            }else{
                it.plot(image, xMapper, yMapper2)
            }
        }

        return image
    }
}