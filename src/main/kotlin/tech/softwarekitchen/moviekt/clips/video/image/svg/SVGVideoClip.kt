package tech.softwarekitchen.moviekt.clips.video.image.svg

import tech.softwarekitchen.common.matrix.Matrix22
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.image.svg.draw.SVGPathDrawer
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.*
import java.awt.*
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.*


data class SVGVideoClipConfiguration(
    val file: File
)

typealias CoordinateMapper = (Double, Double) -> Pair<Double, Double>
typealias Scaler2D = Pair<(Double) -> Double, (Double) -> Double>

fun mergeStyles(parent: List<SVGStyle>, child: List<SVGStyle>): List<SVGStyle>{
    val overridenStyleKeys = child.map{it.type}
    return parent.filter{!overridenStyleKeys.contains(it.type)} + child
}

fun List<SVGStyle>.getFillColor(): Color?{
    val rule = firstOrNull{it.type == SVGStyleType.Fill} ?: return null
    return parseFillColor(rule.value)
}

fun Graphics2D.drawWithSVGStyle(styles: List<SVGStyle>, fillOp: (Graphics2D) -> Unit, strokeOp: (Graphics2D) -> Unit, scaler: Scaler2D){
    val unscaledStrokeWidth = (styles.firstOrNull{it.type == SVGStyleType.StrokeWidth}?.value ?: "1").toDouble()
    val strokeWidth = scaler.first(unscaledStrokeWidth)
    stroke = BasicStroke(strokeWidth.toFloat())

    val fc = styles.firstOrNull { it.type == SVGStyleType.Fill }?.let { parseFillColor(it.value) }

    if (fc != null) {
        color = fc
        fillOp(this)
    }

    val sc = styles.firstOrNull { it.type == SVGStyleType.Stroke }?.let { parseFillColor(it.value) }

    if(sc != null){
        color = sc
        strokeOp(this)
    }
}

fun SVGPath.draw(size: Vector2i, g: Graphics2D,coordinateMapper: CoordinateMapper, scaler: Scaler2D, parentStyles: List<SVGStyle>){
    SVGPathDrawer().draw(this, size, g, coordinateMapper, scaler, parentStyles)
}

fun SVGCircle.draw(g: Graphics2D, scaler: Scaler2D, coordinateMapper: CoordinateMapper, parentStyles: List<SVGStyle>){
    val base = coordinateMapper(center.first, center.second)
    val scaledRadius = scaler.first(radius)

    g.drawWithSVGStyle(
        parentStyles,
        {
            it.fillOval(
                (base.first - scaledRadius).roundToInt(),
                (base.second - scaledRadius).roundToInt(),
                (2*scaledRadius).roundToInt(),
                (2*scaledRadius).roundToInt()
            )
        },
        {
            g.drawOval(
                (base.first - scaledRadius).roundToInt(),
                (base.second - scaledRadius).roundToInt(),
                (2*scaledRadius).roundToInt(),
                (2*scaledRadius).roundToInt(),
            )
        },
        scaler
    )
}

fun SVGGroup.draw(size: Vector2i, g: Graphics2D, scaler: Scaler2D, coordinateMapper: CoordinateMapper, parentStyles: List<SVGStyle>) {
    val effectiveStyles = mergeStyles(parentStyles, styles)
    children.forEach {
        when {
            it is SVGGroup -> it.draw(size, g, scaler, coordinateMapper, effectiveStyles)
            it is SVGPath -> it.draw(size, g,coordinateMapper, scaler, effectiveStyles)
            it is SVGCircle -> it.draw(g, scaler, coordinateMapper, effectiveStyles)
            else -> throw Exception()
        }
    }
}

fun SVGImage.draw(target: BufferedImage){
    val size = Vector2i(target.width, target.height)
    val coordinateMapper: (Double, Double) -> Pair<Double, Double> = getCoordinateMapper(size)
    val scaler = getScaler(size)
    val g = target.createGraphics()

    data.forEach {
        when {
            it is SVGGroup -> it.draw(size, g, scaler, coordinateMapper, styles)
            it is SVGPath -> it.draw(size, g, coordinateMapper, scaler, styles)
            it is SVGCircle -> it.draw(g, scaler, coordinateMapper, styles)
            else -> throw Exception()
        }
    }
}

class SVGVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: SVGVideoClipConfiguration,
): VideoClip(
    id, size, position, visible
) {
    private val content: SVGImage = SVGImage(configuration.file)

    override fun renderContent(img: BufferedImage) {
        content.draw(img)
    }
}

