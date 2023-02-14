package tech.softwarekitchen.moviekt.clips.diagram

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.Color
import java.awt.image.BufferedImage

abstract class XYDiagramClip(
    base: Vector2i, size: Vector2i,
    tOffset: Float, visibilityDuration: Float? = null,
    yAxis: DiagramAxisConfiguration, xAxis: DiagramAxisConfiguration,
    private val configuration: XYDiagramConfiguration
): DiagramClip(
    base, size, tOffset, visibilityDuration, yAxis = yAxis, xAxis = xAxis
) {
    abstract fun getData(): List<Pair<Double,Double>>

    override fun getYLegendEntries(dataScreenHeight: Int): List<LegendEntry> {
        val dataBounds = getDataBounds()
        val max = dataBounds.ymax
        val ceilExponent = Math.ceil(Math.log10(max)).toInt()
        val rel = max / (Math.pow(10.0, ceilExponent.toDouble()))
        //.2, .5 .0
        val intvRel = when{
            rel < .2 -> 0.05
            rel < .5 -> 0.1
            else -> 0.2
        }

        val formatter = getFormatFor10Exp(ceilExponent)
        return (0 until 6).map{intvRel * it}.filter{it < rel}
            .map{
                val legend = when(configuration.yAxis.unit){
                    null -> formatter(it)
                    else -> formatter(it)+configuration.yAxis.unit
                }
                val yPos = ((1.0 - it / rel) * dataScreenHeight).toInt()
                LegendEntry(yPos,legend)
            }
    }

    override fun getXLegendEntries(dataScreenWidth: Int): List<LegendEntry> {
        val dataBounds = getDataBounds()
        val max = dataBounds.xmax
        val ceilExponent = Math.ceil(Math.log10(max)).toInt()
        val rel = max / (Math.pow(10.0, ceilExponent.toDouble()))
        //.2, .5 .0
        val intvRel = when{
            rel < .2 -> 0.05
            rel < .5 -> 0.1
            else -> 0.2
        }

        val formatter = getFormatFor10Exp(ceilExponent)
        return (0 until 6).map{intvRel * it}.filter{it < rel}
            .map{
                val legend = when(configuration.xAxis.unit){
                    null -> formatter(it)
                    else -> formatter(it)+configuration.xAxis.unit
                }
                val yPos = ((it / rel) * dataScreenWidth).toInt()
                LegendEntry(yPos,legend)
            }
    }

    protected class DataBounds(val xmin: Double, val ymin: Double, val xmax: Double, val ymax: Double)
    protected fun getDataBounds(): DataBounds{
        val data = getData()
        val xmin = when(configuration.xAxis.min) {
            null ->
                try{
                    data.minOf { it.first }
                }catch(ex: java.lang.Exception){
                    0.0
                }
            else -> configuration.xAxis.min
        }
        val xmax = when(configuration.xAxis.max) {
            null ->
                try {
                    data.maxOf{it.second}
                } catch (ex: Exception) {
                    1.0
                }
            else -> configuration.xAxis.max
        }
        val ymin = when(configuration.yAxis.min) {
            null ->
                try{
                    data.minOf { it.second }
                }catch(ex: java.lang.Exception){
                    0.0
                }
            else -> configuration.yAxis.min
        }
        val ymax = when(configuration.yAxis.max) {
            null ->
                try {
                    data.maxOf{it.second}
                } catch (ex: Exception) {
                    1.0
                }
            else -> configuration.yAxis.max
        }
        return DataBounds(xmin,ymin,xmax,ymax)
    }

    protected fun getFormatFor10Exp(exp10: Int): (Double) -> String{
        val prefixCtr = (exp10 - 1) / 3
        val prefix = when(prefixCtr){
            4 -> "T"
            3 -> "G"
            2 -> "M"
            1 -> "k"
            0 -> {
                if(exp10 < 0){
                    "m"
                }else{
                    ""
                }
            }
            -1 -> "µ"
            -2 -> "n"
            -3 -> "p"
            -4 -> "f"
            else -> throw Exception()
        }

        return when(exp10 % 3){
            0 -> {{String.format("%.0f",it*1000) + prefix}}
            1 -> {{String.format("%.2f",it*10) + prefix}}
            2 -> {{String.format("%.1f",it*100) + prefix}}
            else -> throw Exception() //awkward
        }
    }

    protected fun drawBackgroundGrid(image: BufferedImage){
        val graphics = image.createGraphics()
        when(configuration.grid){
            DynamicLineDiagramBackgroundGrid.None -> {}
            DynamicLineDiagramBackgroundGrid.Y -> {
                graphics.color = Color(255,255,255,128)
                for(item in getYLegendEntries(image.height)){
                    graphics.fillRect(0,item.pos-1,size.x , 3)
                }
            }
        }
    }
}