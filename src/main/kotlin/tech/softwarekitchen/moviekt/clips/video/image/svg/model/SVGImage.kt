package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import java.io.File

class SVGImage(file: File) {
    val basePos: Pair<Int, Int>
    val canvasSize: Pair<Int, Int>
    val base: SVGGroup

    init {
        val res = file.readText()
        val parsed = XmlMapper().readValue(res, Map::class.java)

        if (parsed.keys.contains("viewBox")) {
            val viewBase = (parsed["viewBox"] as String).split(" ")
            basePos = Pair(viewBase[0].toInt(), viewBase[1].toInt())
            canvasSize = Pair(viewBase[2].toInt(), viewBase[3].toInt())
        } else {
            //Go via width & height
            val wid = (parsed["width"] as String).toInt()
            val hei = (parsed["height"] as String).toInt()
            basePos = Pair(0, 0)
            canvasSize = Pair(wid, hei)
        }

        base = SVGGroup(parsed as Map<String, Any>)
    }

    fun getCoordinateMapper(size: Vector2i): (Double, Double) -> Pair<Double, Double>{
        return {
                x, y ->
            Pair(
                size.x * (x - basePos.first) / canvasSize.first,
                size.y * (y - basePos.second) / canvasSize.second
            )
        }
    }

    fun getScaler(size: Vector2i): Scaler2D{
        val facX = size.x.toDouble() / canvasSize.first
        val facY = size.y.toDouble() / canvasSize.second
        return Pair({it * facX}, {it * facY})
    }
}