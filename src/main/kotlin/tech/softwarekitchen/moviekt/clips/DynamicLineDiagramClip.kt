package tech.softwarekitchen.moviekt.clips

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.Shape
import java.awt.image.BufferedImage

enum class DynamicLineDiagramAxisMode{
    None, Default
}

enum class DynamicLineDiagramBackgroundGrid{
    None, Y
}

data class DynamicLineAxisConfiguration(
    val axisMode: DynamicLineDiagramAxisMode = DynamicLineDiagramAxisMode.None,
    val axisHeight: Double? = null,
    val unit: String = ""
)
data class DynamicLineBaseConfiguration(
    val fixedDataPoints: Int? = null
)
data class DynamicLineDiagramColorConfiguration(
    val underGraphColor: Color? = null
)
data class DynamicLineDiagramClipConfiguration(
    val yAxis: DynamicLineAxisConfiguration = DynamicLineAxisConfiguration(),
    val baseAxis: DynamicLineBaseConfiguration = DynamicLineBaseConfiguration(),
    val grid: DynamicLineDiagramBackgroundGrid = DynamicLineDiagramBackgroundGrid.None,
    val colors: DynamicLineDiagramColorConfiguration = DynamicLineDiagramColorConfiguration()
)

class DynamicLineDiagramClip(
    base: Vector2i,
    size: Vector2i,
    private val dataProvider: () -> List<Double>,
    private val configuration: DynamicLineDiagramClipConfiguration = DynamicLineDiagramClipConfiguration(),
    tOffset: Float = 0f,
    visibilityDuration: Float? = null,
): Clip(base, size, tOffset, visibilityDuration) {
    private val padding: Vector2i
    init{
        padding = when(configuration.yAxis.axisMode){
            DynamicLineDiagramAxisMode.None -> Vector2i(0,0)
            DynamicLineDiagramAxisMode.Default -> Vector2i(70,20)
        }
        if(size.x < padding.x || size.y < padding.y){
            throw Exception()
        }
    }

    private fun getMax(data: List<Double>): Double{
        return when(configuration.yAxis.axisHeight) {
            null ->
                try {
                    data.max()
                } catch (ex: Exception) {
                    1.0
                }
            else -> configuration.yAxis.axisHeight
        }
    }

    private fun drawData(data: List<Double>, graphics: Graphics2D){
        graphics.color = Color(255,0,0,255)
        val max = getMax(data)
        val xReference = when(configuration.baseAxis.fixedDataPoints){
            null -> data.size
            else -> configuration.baseAxis.fixedDataPoints
        }
        val dataScreenWidth = size.x - padding.x
        val dataScreenHeight = size.y - padding.y

        configuration.colors.underGraphColor?.let{
            graphics.color = it
            for(i in 1 until data.size){
                val shape = Polygon()
                shape.addPoint(padding.x + (i-1) * dataScreenWidth / xReference, dataScreenHeight)
                shape.addPoint(padding.x + (i-1) * dataScreenWidth / xReference, (dataScreenHeight * (1.0 - (data[i-1] / max))).toInt())
                shape.addPoint(padding.x + i * dataScreenWidth / xReference, (dataScreenHeight * (1.0 - (data[i] / max))).toInt())
                shape.addPoint(padding.x + i * dataScreenWidth / xReference, dataScreenHeight)
                graphics.fill(shape)
            }
        }
        graphics.setStroke(BasicStroke(3f))
        for(i in 1 until data.size){
            graphics.drawLine(
                padding.x + (i-1) * dataScreenWidth / xReference,
                (dataScreenHeight * (1.0 - (data[i-1] / max))).toInt(),
                padding.x + i*dataScreenWidth / xReference,
                (dataScreenHeight * (1.0 - (data[i] / max))).toInt()
            )
        }
        graphics.setStroke(BasicStroke(1f))
    }

    private fun getFormatFor10Exp(exp10: Int): (Double) -> String{
        //0 - 9.99 -> exp=1 unit=-
        //10 -     -> exp=2 unit=-
        //100 -    -> exp=3 unit=-
        //1000 -   -> exp=4 unit=k

        val prefixCtr = (exp10 - 1) / 3
        val prefix = when(prefixCtr){
            4 -> "T"
            3 -> "G"
            2 -> "M"
            1 -> "k"
            0 -> ""
            -1 -> "m"
            -2 -> "µ"
            -3 -> "n"
            -4 -> "p"
            else -> throw Exception()
        }

        return when(exp10 % 3){
            0 -> {{String.format("%.0f",it*1000) + prefix}}
            1 -> {{String.format("%.2f",it*10) + prefix}}
            2 -> {{String.format("%.1f",it*100) + prefix}}
            else -> throw Exception() //awkward
        }
    }

    data class YLegendEntry(val yPos: Int, val legend: String)
    private fun getYLegendEntries(data: List<Double>): List<YLegendEntry>{
        val dataScreenHeight = size.y - padding.y
        val max = getMax(data)
        val ceilExponent = Math.ceil(Math.log10(max)).toInt()

        val rel = max / (Math.pow(10.0, ceilExponent.toDouble()))
        //.2, .5 .0
        val intvRel = when{
            rel < .2 -> 0.05
            rel < .5 -> 0.1
            else -> 0.2
        }

        val formatter = getFormatFor10Exp(ceilExponent)
        return (0 until 4).map{intvRel * it}.filter{it < rel}
            .map{
                val legend = when(configuration.yAxis.unit){
                    null -> formatter(it)
                    else -> formatter(it)+configuration.yAxis.unit
                }
                val yPos = ((1.0 - it / rel) * dataScreenHeight).toInt()
                YLegendEntry(yPos,legend)
            }
    }

    private fun drawYAxis(data: List<Double>, graphics: Graphics2D){
        val dataScreenHeight = size.y - padding.y

        when(configuration.yAxis.axisMode){
            DynamicLineDiagramAxisMode.None -> {}
            DynamicLineDiagramAxisMode.Default -> {
                graphics.color = Color(255,255,255,255)

                val yAxisEntries = getYLegendEntries(data)
                for(item in yAxisEntries){
                    graphics.fillRect(padding.x-7,item.yPos-2,7,5)
                    graphics.drawString(item.legend,2,item.yPos+8)
                }
                graphics.drawLine(padding.x,0,padding.x,dataScreenHeight)
            }
        }
    }

    private fun drawBackgroundGrid(data: List<Double>, graphics: Graphics2D){
        when(configuration.grid){
            DynamicLineDiagramBackgroundGrid.None -> {}
            DynamicLineDiagramBackgroundGrid.Y -> {
                graphics.color = Color(255,255,255,128)
                for(item in getYLegendEntries(data)){
                    graphics.fillRect(padding.x,item.yPos-1,size.x - padding.x, 3)
                }
            }
        }
    }

    override fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage {
        val data = dataProvider()
        val img = BufferedImage(size.x, size.y,BufferedImage.TYPE_INT_ARGB)
        val graphics = img.createGraphics()
        drawBackgroundGrid(data, graphics)
        drawData(data, graphics)
        drawYAxis(data, graphics)
        return img
    }
}
