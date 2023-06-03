package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import org.w3c.dom.Element
import tech.softwarekitchen.moviekt.util.parseColor
import java.awt.Color

fun parseFillColor(color: String): Color?{
    if(color == "none"){
        return null
    }
    return parseColor(color)
}

enum class SVGStyleType{
    Fill, Stroke, StrokeWidth
}
data class SVGStyle(val type: SVGStyleType, val value: String)

fun Element.parseSVGStyles(): List<SVGStyle>{
    val styleString = attributes.getNamedItem("style")?.textContent

    styleString ?: return listOf()
    val styles = ArrayList<SVGStyle>()

    styleString.let{
            styleString ->
        val statements = styleString.split(";").map(String::trim).filter{!it.isBlank()}
        statements.forEach{
                stmt ->
            val parts = stmt.split(":").map(String::trim).filter{!it.isBlank()}
            when(val key = parts[0].lowercase()){
                "fill" -> styles.add(SVGStyle(SVGStyleType.Fill, parts[1]))
                "stroke" -> styles.add(SVGStyle(SVGStyleType.Stroke, parts[1]))
                "stroke-width" -> styles.add(SVGStyle(SVGStyleType.StrokeWidth, parts[1]))
                else -> {
                    println("Warning: Ignoring SVG style key '$key'")
                }
            }
        }
    }

    return styles
}
