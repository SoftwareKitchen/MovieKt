package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.w3c.dom.Element
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

interface SVGItem

fun Element.svgIterate(): List<SVGItem>{
    val result = ArrayList<SVGItem?>()
    (0 until childNodes.length).map{
        val node = childNodes.item(it)
        if(node is Element){
            result.add(when(node.tagName){
                "g" -> SVGGroup(node)
                "path" -> SVGPath.fromXMLNode(node)
                "circle" -> SVGCircle(node)
                "defs" -> SVGGroup(node)    //Fixme?
                "rect" -> null              //TODO
                "clipPath" -> null          //TODO
                else -> throw Exception()
            })
        }
    }
    return result.filterNotNull()
}

class SVGImage(file: File) {
    val basePos: Pair<Int, Int>
    val canvasSize: Pair<Int, Int>
    val data: List<SVGItem>
    val styles: List<SVGStyle>

    init {
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val xmlDoc = builder.parse(file.inputStream())
        val svgElem = xmlDoc.documentElement

        styles = svgElem.parseSVGStyles()

        val viewBox = svgElem.attributes.getNamedItem("viewBox")
        if (viewBox != null) {
            val vb = viewBox.textContent
            val viewBase = vb.split(" ")
            basePos = Pair(viewBase[0].toInt(), viewBase[1].toInt())
            canvasSize = Pair(viewBase[2].toInt(), viewBase[3].toInt())
        } else {
            //Go via width & height

            val wid = (svgElem.attributes.getNamedItem("width").textContent).toInt()
            val hei = (svgElem.attributes.getNamedItem("height").textContent).toInt()
            basePos = Pair(0, 0)
            canvasSize = Pair(wid, hei)
        }

        data = svgElem.svgIterate()
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