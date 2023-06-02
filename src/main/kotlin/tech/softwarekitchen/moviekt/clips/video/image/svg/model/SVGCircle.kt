package tech.softwarekitchen.moviekt.clips.video.image.svg.model

class SVGCircle(source: Map<String, Any>) {
    val center: Pair<Double, Double>
    val radius: Double

    init{
        center = Pair(
            (source["cx"] as String).toDouble(),
            (source["cy"] as String).toDouble()
        )
        radius = (source["r"] as String).toDouble()
    }
}
