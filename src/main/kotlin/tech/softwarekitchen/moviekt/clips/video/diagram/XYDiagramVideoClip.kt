package tech.softwarekitchen.moviekt.clips.video.diagram

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.position.SizeProvider
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramBackgroundGrid
import tech.softwarekitchen.moviekt.clips.video.diagram.impl.DynamicLineDiagramColorConfiguration

open class XYDiagramConfiguration(
    val xAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    val yAxis: DiagramAxisConfiguration = DiagramAxisConfiguration(),
    val grid: DynamicLineDiagramBackgroundGrid = DynamicLineDiagramBackgroundGrid.None,
    val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
)

abstract class XYDiagramVideoClip(
    size: SizeProvider,
    tOffset: Float, visibilityDuration: Float? = null,
    yAxis: DiagramAxisConfiguration, xAxis: DiagramAxisConfiguration,
    private val configuration: XYDiagramConfiguration
): DiagramVideoClip(
    size, tOffset, visibilityDuration, yAxis = yAxis, xAxis = xAxis
) {
    abstract fun getData(): List<Pair<Double,Double>>


    private fun generateLinearBounds(min: Double, max: Double, pixSize: Int, invert: Boolean = false): List<LegendEntry>{
        val ceilExponent = Math.ceil(Math.log10(max-min)).toInt()
        val rel = (max - min) / (Math.pow(10.0, ceilExponent.toDouble()))
        val relMax = max / (Math.pow(10.0, ceilExponent.toDouble()))
        val relMin = min / (Math.pow(10.0, ceilExponent.toDouble()))

        val intvRel = when{
            rel < .2 -> 0.05
            rel < .5 -> 0.1
            else -> 0.2
        }

        return (0 until 6).map{intvRel * it}.filter{(it <= relMax) && (it >= relMin)}
            .map{
                val pos = when(invert) {
                    false -> (((it - relMin) / rel) * pixSize).toInt()
                    true -> ((1.0 - (it - relMin) / rel) * pixSize).toInt()
                }
                LegendEntry(pos, formatExp10(it * Math.pow(10.0,ceilExponent.toDouble())))
            }
    }

    private fun generateLogarithmicBounds(min: Double, max: Double, pixSize: Int, invert: Boolean = false): List<LegendEntry>{
        val expMin = Math.log10(min)
        val expMax = Math.log10(max)
        val innerExponentLow = Math.ceil(expMin).toInt()
        val upperExponentLow = Math.ceil(expMax).toInt()

        return (innerExponentLow..upperExponentLow).map{
            val quot = when(invert) {
                false -> (it - expMin) / (expMax - expMin)
                true -> 1 - (it - expMin) / (expMax - expMin)
            }
            LegendEntry((quot * pixSize).toInt(),formatExp10(Math.pow(10.0,it.toDouble())))
        }
    }

    override fun getYLegendEntries(dataScreenHeight: Int): List<LegendEntry> {
        val dataBounds = getDataBounds()
        val min = dataBounds.ymin
        val max = dataBounds.ymax
        val unit = when(val v = configuration.yAxis.unit){
            null -> ""
            else -> v
        }
        if(configuration.yAxis.mode == DiagramAxisMode.Logarithmic){
            return generateLogarithmicBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
        }
        return generateLinearBounds(min,max,dataScreenHeight, invert=true).map{ LegendEntry(it.pos, it.legend+unit) }
    }

    override fun getXLegendEntries(dataScreenWidth: Int): List<LegendEntry> {
        val dataBounds = getDataBounds()
        val max = dataBounds.xmax
        val min = dataBounds.xmin
        val unit = when(val v = configuration.xAxis.unit){
            null -> ""
            else -> v
        }
        if(configuration.xAxis.mode == DiagramAxisMode.Logarithmic){
            return generateLogarithmicBounds(min, max, dataScreenWidth).map{ LegendEntry(it.pos, it.legend+unit) }
        }
        return generateLinearBounds(min, max, dataScreenWidth).map{ LegendEntry(it.pos, it.legend+unit) }
    }

    protected fun formatExp10(v: Double): String{
        val ceilExponent = if(v == 0.0) {
            0
        }else{
            Math.ceil(Math.log10(v)).toInt()
        }
        val rel = v / (Math.pow(10.0, ceilExponent.toDouble()))
        //.2, .5 .0

        val prefixCtr = (ceilExponent - 1) / 3
        val prefix = when(prefixCtr){
            4 -> "T"
            3 -> "G"
            2 -> "M"
            1 -> "k"
            0 -> {
                if(ceilExponent < 0){
                    "m"
                }else{
                    ""
                }
            }
            -1 -> "µ"
            -2 -> "n"
            -3 -> "p"
            -4 -> "f"
            -5 -> "a"
            else -> {
                throw Exception()
            }
        }

        return when(ceilExponent % 3){
            0 -> String.format("%.0f",rel*1000) + prefix
            -2, 1 -> String.format("%.2f",rel*10) + prefix
            -1, 2 -> String.format("%.1f",rel*100) + prefix
            else -> throw Exception() //awkward
        }
    }

    protected class DataBounds(val xmin: Double, val ymin: Double, val xmax: Double, val ymax: Double)
    protected fun getDataBounds(): DataBounds {
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
                    data.maxOf{it.first}
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
}
