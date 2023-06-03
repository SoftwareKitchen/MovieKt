package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import org.w3c.dom.Element

class SVGCircle(source: Element): SVGItem {
    val center: Pair<Double, Double>
    val radius: Double

    init{
        center = Pair(
            source.attributes.getNamedItem("cx")!!.textContent.toDouble(),
            source.attributes.getNamedItem("cy")!!.textContent.toDouble()
        )
        radius = source.attributes.getNamedItem("r")!!.textContent.toDouble()

    }
}
