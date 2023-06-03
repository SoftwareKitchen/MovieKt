package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import org.w3c.dom.Element

class SVGGroup(data: Element): SVGItem {
    val children: List<SVGItem>
    val styles: List<SVGStyle>

    init{
        children = data.svgIterate()
        styles = data.parseSVGStyles()
    }
}