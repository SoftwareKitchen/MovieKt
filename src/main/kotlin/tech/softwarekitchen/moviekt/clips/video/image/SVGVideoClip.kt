package tech.softwarekitchen.moviekt.clips.video.image

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import tech.softwarekitchen.common.matrix.Matrix22
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.util.parseColor
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Stroke
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.*

private fun parseFillColor(color: String): Color?{
    if(color == "none"){
        return null
    }
    return parseColor(color)
}

data class SVGVideoClipConfiguration(
    val file: File,
    val strokeColor: Color = Color.BLACK,
    val fillColor: Color? = null,
    val stroke: Stroke = BasicStroke(1f)
)

enum class SVGOperationType{
    Move, RelativeMove, CubicBezier, Line, RelativeLine, Arc, RelativeArc, HorizontalLine, RelativeHorizontalLine, VerticalLine, RelativeVerticalLine, ClosePath
}

interface SVGOperation{
    val type: SVGOperationType
}

class SVGLineOperation(val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.Line
}
class SVGRelativeLineOperation(val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.RelativeLine
}
class SVGCubicBezierOperation(val b1: Pair<Double, Double>, val b2: Pair<Double, Double>, val end: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.CubicBezier
}
class SVGMoveOperation(val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.Move
}
class SVGRelativeMoveOperation(val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.RelativeMove
}
class SVGArcOperation(val rad: Pair<Double, Double>, val xDeg: Double, val flgLongArc: Int, val flgDir: Int, val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.Arc
}
class SVGRelativeArcOperation(val rad: Pair<Double, Double>, val xDeg: Double, val flgLongArc: Int, val flgDir: Int, val target: Pair<Double, Double>): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.RelativeArc
}
class SVGHorizontalLine(val dx: Double): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.HorizontalLine
}
class SVGRelativeHorizontalLine(val dx: Double): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.RelativeHorizontalLine
}

class SVGVerticalLine(val dy: Double): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.VerticalLine
}

class SVGRelativeVerticalLine(val dy: Double): SVGOperation{
    override val type: SVGOperationType = SVGOperationType.RelativeVerticalLine
}

class SVGClosePath: SVGOperation{
    override val type: SVGOperationType = SVGOperationType.ClosePath
}

enum class SVGStyleType{
    Fill, Stroke
}
data class SVGStyle(val type: SVGStyleType, val value: String)
data class SVGPath(
    val ops: List<SVGOperation>,
    val style: List<SVGStyle>
)

interface SVGImage{
    fun getOperations(): List<SVGPath>
}
private class SVGGroupReader(private val data: Map<String, Any>): SVGImage{

    override fun getOperations(): List<SVGPath> {
        val path = data["path"] as List<Map<String, Any>>
        val readPaths = path.map{
            SVGPathReader(it["d"] as String, it["style"]?.let{it as String})
        }
        return readPaths.map{it.getOperations()}.flatten()
    }
}

private class

private class SVGPathReader(private val path: String, private val style: String?): SVGImage{
    private val operations: List<SVGOperation>
    private val styles: List<SVGStyle>

    override fun getOperations(): List<SVGPath> {
        return listOf(SVGPath(operations, styles))
    }

    init{
        val ops = ArrayList<SVGOperation>()
        var rest = path

        val splitOffNumber: () -> Double = {
            val negate  = when(rest[0] == '-'){
                true -> {
                    rest = rest.substring(1,rest.length)
                    -1
                }
                else -> +1
            }
            val firstIndex = rest.indexOfFirst { !it.isDigit() && it != '.' }
            if(firstIndex < 0){
                val v = negate * rest.toDouble()
                rest = ""
                v
            }else{
                val part = rest.substring(0,firstIndex)
                rest = rest.substring(firstIndex+1, rest.length).trim()
                negate * part.toDouble()
            }
        }
        val readCooPair: () -> Pair<Double, Double> = {
            Pair(splitOffNumber(), splitOffNumber())
        }
        val readFlag: () -> Int = {
            val c = rest.substring(0,1)
            rest = rest.substring(1,rest.length).trim()
            c.toInt()
        }

        while(rest != ""){
            if(rest[0].isLetter()){
                val letter = rest[0]
                rest = rest.substring(1, rest.length).trim()

                val op = when(letter){
                    'C' -> SVGCubicBezierOperation(readCooPair(), readCooPair(), readCooPair())
                    'M' -> SVGMoveOperation(readCooPair())
                    'm' -> SVGRelativeMoveOperation(readCooPair())
                    'A' -> {
                        SVGArcOperation(readCooPair(),splitOffNumber(), readFlag(), readFlag(),readCooPair())
                    }
                    'a' -> {
                        SVGRelativeArcOperation(readCooPair(),splitOffNumber(), readFlag(), readFlag(),readCooPair())
                    }
                    'L' -> {
                        SVGLineOperation(readCooPair())
                    }
                    'l' -> {
                        SVGRelativeLineOperation(readCooPair())
                    }
                    'H' -> {
                        SVGHorizontalLine(splitOffNumber())
                    }
                    'h' -> {
                        SVGRelativeHorizontalLine(splitOffNumber())
                    }
                    'V' -> {
                        SVGVerticalLine(splitOffNumber())
                    }
                    'v' -> {
                        SVGRelativeVerticalLine(splitOffNumber())
                    }
                    'Z', 'z' -> {
                        SVGClosePath()
                    }
                    else -> throw Exception()
                }
                ops.add(op)
            }else{
                if(ops.last().type == SVGOperationType.RelativeLine){
                    ops.add(SVGRelativeLineOperation(readCooPair()))
                }else{
                    ops.add(SVGLineOperation(readCooPair()))
                }
            }
        }

        if(ops.last() !is SVGClosePath){
            ops.add(SVGClosePath())
        }

        operations = ops

        val styles = ArrayList<SVGStyle>()

        style?.let{
            styleString ->
            val statements = styleString.split(";").map(String::trim).filter{!it.isBlank()}
            statements.forEach{
                stmt ->
                val parts = stmt.split(":").map(String::trim).filter{!it.isBlank()}
                when(val key = parts[0].lowercase()){
                    "fill" -> styles.add(SVGStyle(SVGStyleType.Fill, parts[1]))
                    "stroke" -> styles.add(SVGStyle(SVGStyleType.Stroke, parts[1]))
                    else -> {
                        println("Warning: Ignoring SVG style key '$key'")
                    }
                }
            }
        }

        this.styles = styles
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
    private val basePos: Pair<Int, Int>
    private val canvasSize: Pair<Int, Int>
    private val content: SVGImage

    init{
        val res = configuration.file.readText()
        val parsed = XmlMapper().readValue(res,Map::class.java)


        if(parsed.keys.contains("viewBox")){
            val viewBase = (parsed["viewBox"] as String).split(" ")
            basePos = Pair(viewBase[0].toInt(), viewBase[1].toInt())
            canvasSize = Pair(viewBase[2].toInt(), viewBase[3].toInt())
        }else{
            //Go via width & height
            val wid = (parsed["width"] as String).toInt()
            val hei = (parsed["height"] as String).toInt()
            basePos = Pair(0,0)
            canvasSize = Pair(wid, hei)
        }
        content = when{
            "path" in parsed.keys -> {
                val pathData = (parsed["path"] as Map<String, Any>)["d"]!! as String
                val styleData = (parsed["path"] as Map<String, Any>)["style"]?.let{it as String}
                SVGPathReader(pathData, styleData)
            }
            "g" in parsed.keys -> SVGGroupReader(parsed["g"] as Map<String, Any>)
            else -> throw Exception()
        }

    }

    override fun renderContent(img: BufferedImage) {
        val size = Vector2i(img.width, img.height)
        val coordinateMapper: (Double, Double) -> Pair<Double, Double> = {
            x, y ->
            Pair(
                size.x * (x - basePos.first) / canvasSize.first,
                size.y * (y - basePos.second) / canvasSize.second
            )
        }

        val g = img.createGraphics()

        g.color = Color.LIGHT_GRAY
        g.fillRect(0,0,img.width, img.height)
        g.stroke = configuration.stroke

        content.getOperations().forEach{
            ops ->
            var path = GeneralPath()
            ops.ops.forEach{
            when(it.type) {
                SVGOperationType.Move -> {
                    val fc = configuration.fillColor
                        ?: ops.style.firstOrNull{it.type == SVGStyleType.Fill}?.let{ parseFillColor(it.value) }

                    if (fc != null) {
                        g.color = fc
                        g.fill(path)
                    }

                    val sc = ops.style.firstOrNull{it.type == SVGStyleType.Stroke}?.let{ parseFillColor(it.value) } ?: configuration.strokeColor

                    g.color = sc
                    g.draw(path)

                    val (mx, my) = coordinateMapper((it as SVGMoveOperation).target.first, it.target.second)
                    path = GeneralPath()
                    path.moveTo(mx, my)
                }
                SVGOperationType.ClosePath -> {
                    val fc = configuration.fillColor
                        ?: ops.style.firstOrNull{it.type == SVGStyleType.Fill}?.let{ parseFillColor(it.value) }

                    if (fc != null) {
                        g.color = fc
                        g.fill(path)
                    }

                    val sc = ops.style.firstOrNull{it.type == SVGStyleType.Stroke}?.let{ parseFillColor(it.value) } ?: configuration.strokeColor
                    g.color = sc
                    g.draw(path)
                }


                SVGOperationType.RelativeMove -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeMoveOperation).target.first, it.target.second)
                    val last = path.currentPoint
                    path = GeneralPath()
                    path.moveTo(mx + last.x, my + last.y)
                }

                SVGOperationType.Line -> {
                    val (mx, my) = coordinateMapper((it as SVGLineOperation).target.first, it.target.second)
                    path.lineTo(mx, my)
                }

                SVGOperationType.RelativeLine -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeLineOperation).target.first, it.target.second)
                    val last = path.currentPoint
                    path.lineTo(mx + last.x, my + last.y)
                }

                SVGOperationType.Arc -> {
                    val (mx, my) = coordinateMapper((it as SVGArcOperation).target.first, it.target.second)
                    val _last = path.currentPoint

                    val last = Vector2(_last.x, _last.y)
                    val tgt = Vector2(mx, my)

                    val rotMatrix = Matrix22.getRotationMatrix(-it.xDeg * PI / 180.0)
                    val scaleMatrix = Matrix22.getScaleMatrix(1 / it.rad.first, 1 / it.rad.second)

                    val lastTransformed = scaleMatrix.mul(rotMatrix.mul(last))
                    val tgtTransformed = scaleMatrix.mul(rotMatrix.mul(tgt))

                    val center = tgtTransformed.plus(lastTransformed).scale(0.5)
                    val d = tgtTransformed.minus(lastTransformed).length()
                    val uni = tgtTransformed.minus(lastTransformed).uni()

                    val orthoMatrix = when (it.flgDir == it.flgLongArc) {
                        true -> Matrix22(0.0, 1.0, -1.0, 0.0)
                        false -> Matrix22(0.0, -1.0, 1.0, 0.0)
                    }
                    val orthoUni = orthoMatrix.mul(uni)
                    val fac = sqrt(1 - 0.25 * d * d)
                    val ellipseCenter = center.plus(orthoUni.scale(fac))

                    val v0 = lastTransformed.minus(ellipseCenter)
                    val v1 = tgtTransformed.minus(ellipseCenter)

                    val angle0 = atan2(v0.y, v0.x)
                    val angle1 = atan2(v1.y, v1.x)

                    val dir01 = angle1 - angle0
                    val circ = 2 * PI
                    val (base, step, end) = when {
                        dir01 >= PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 >= PI -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 && it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 > -PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0)
                        dir01 > -PI -> Triple(angle0, 0.1, angle1 + circ)
                        it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1 + circ)
                        else -> Triple(angle1, 0.1, angle0)
                    }

                    val numPoints = floor((end - base) / step).toInt()
                    val points = (0 until numPoints).map { base + (it + 1) * step }.map { Vector2(cos(it), sin(it)) }
                        .map { it.plus(ellipseCenter) }

                    val rotMatrixInverted = rotMatrix.invert()
                    val scaleMatrixInverted = scaleMatrix.invert()
                    val pointsExtracted = points.map { rotMatrixInverted.mul(scaleMatrixInverted.mul(it)) }

                    pointsExtracted.forEach {
                        path.lineTo(it.x, it.y)
                    }
                    path.lineTo(tgt.x, tgt.y)
                }      //FIXME, duplication..
                SVGOperationType.RelativeArc -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeArcOperation).target.first, it.target.second)
                    val _last = path.currentPoint

                    val last = Vector2(_last.x, _last.y)
                    val tgt = last.plus(Vector2(mx, my))

                    val rotMatrix = Matrix22.getRotationMatrix(-it.xDeg * PI / 180.0)
                    val scaleMatrix = Matrix22.getScaleMatrix(1 / it.rad.first, 1 / it.rad.second)

                    val lastTransformed = scaleMatrix.mul(rotMatrix.mul(last))
                    val tgtTransformed = scaleMatrix.mul(rotMatrix.mul(tgt))

                    val center = tgtTransformed.plus(lastTransformed).scale(0.5)
                    val d = tgtTransformed.minus(lastTransformed).length()
                    val uni = tgtTransformed.minus(lastTransformed).uni()

                    val orthoMatrix = when (it.flgDir == it.flgLongArc) {
                        true -> Matrix22(0.0, 1.0, -1.0, 0.0)
                        false -> Matrix22(0.0, -1.0, 1.0, 0.0)
                    }
                    val orthoUni = orthoMatrix.mul(uni)
                    val fac = sqrt(1 - 0.25 * d * d)
                    val ellipseCenter = center.plus(orthoUni.scale(fac))

                    val v0 = lastTransformed.minus(ellipseCenter)
                    val v1 = tgtTransformed.minus(ellipseCenter)

                    val angle0 = atan2(v0.y, v0.x)
                    val angle1 = atan2(v1.y, v1.x)

                    val dir01 = angle1 - angle0
                    val circ = 2 * PI
                    val (base, step, end) = when {
                        dir01 >= PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 >= PI -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 && it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 > -PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0)
                        dir01 > -PI -> Triple(angle0, 0.1, angle1 + circ)
                        it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1 + circ)
                        else -> Triple(angle1, 0.1, angle0)
                    }

                    val numPoints = floor((end - base) / step).toInt()
                    val points = (0 until numPoints).map { base + (it + 1) * step }.map { Vector2(cos(it), sin(it)) }
                        .map { it.plus(ellipseCenter) }

                    val rotMatrixInverted = rotMatrix.invert()
                    val scaleMatrixInverted = scaleMatrix.invert()
                    val pointsExtracted = points.map { rotMatrixInverted.mul(scaleMatrixInverted.mul(it)) }

                    pointsExtracted.forEach {
                        path.lineTo(it.x, it.y)
                    }
                    path.lineTo(tgt.x, tgt.y)
                }

                SVGOperationType.HorizontalLine -> {
                    val prev = path.currentPoint
                    val (tgtX, _) = coordinateMapper((it as SVGHorizontalLine).dx, 0.0)
                    path.lineTo(tgtX, prev.y)
                }

                SVGOperationType.RelativeHorizontalLine -> {
                    val prev = path.currentPoint
                    val (tgtX, _) = coordinateMapper((it as SVGRelativeHorizontalLine).dx, 0.0)
                    path.lineTo(prev.x + tgtX, prev.y)
                }

                SVGOperationType.VerticalLine -> {
                    val prev = path.currentPoint
                    val (_, tgtY) = coordinateMapper(0.0, (it as SVGVerticalLine).dy)
                    path.lineTo(prev.x, tgtY)
                }

                SVGOperationType.RelativeVerticalLine -> {
                    val prev = path.currentPoint
                    val (_, tgtY) = coordinateMapper(0.0, (it as SVGRelativeVerticalLine).dy)
                    path.lineTo(prev.x, tgtY + prev.y)
                }
                SVGOperationType.CubicBezier -> {
                    val cbDesc = it as SVGCubicBezierOperation
                    val b1 = coordinateMapper(cbDesc.b1.first, cbDesc.b1.second)
                    val b2 = coordinateMapper(cbDesc.b2.first, cbDesc.b2.second)
                    val end = coordinateMapper(cbDesc.end.first, cbDesc.end.second)
                    path.curveTo(
                        b1.first, b1.second,
                        b2.first, b2.second,
                        end.first, end.second
                    )
                }
            }
            }
        }
    }
}

