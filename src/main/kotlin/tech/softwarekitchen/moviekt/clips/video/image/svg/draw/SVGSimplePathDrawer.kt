package tech.softwarekitchen.moviekt.clips.video.image.svg.draw

import tech.softwarekitchen.common.matrix.Matrix22
import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.CoordinateMapper
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import tech.softwarekitchen.moviekt.clips.video.image.svg.drawWithSVGStyle
import tech.softwarekitchen.moviekt.clips.video.image.svg.mergeStyles
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.*
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.GeneralPath
import java.awt.geom.Point2D
import kotlin.math.*

class SVGSimplePathDrawer: SVGPartialDrawer<SVGPath> {

    override fun draw(
        it: SVGPath,
        size: Vector2i,
        g: Graphics2D,
        coordinateMapper: CoordinateMapper,
        scaler: Scaler2D,
        parentStyles: List<SVGStyle>
    ) {
        drawCustom(
            it,size, g, coordinateMapper, scaler, parentStyles
        )
    }


    fun drawCustom(
        it: SVGPath,
        size: Vector2i,
        g: Graphics2D,
        coordinateMapper: CoordinateMapper,
        scaler: Scaler2D,
        parentStyles: List<SVGStyle>,
        skipFill: Boolean = false
    ){
        val effectiveStyles = mergeStyles(parentStyles, it.styles)

        var path: GeneralPath? = null

        var lastOp: SVGOperation? = null
        var lastOrigin: Point2D? = null
        it.operations.forEach {
            val origin = path?.currentPoint
            when (it.type) {
                SVGOperationType.Move -> {
                    val (mx, my) = coordinateMapper((it as SVGMoveOperation).target.first, it.target.second)
                    if(path == null){
                        path = GeneralPath()
                    }
                    path!!.moveTo(mx, my)
                }

                SVGOperationType.ClosePath -> {
                    path!!.closePath()
                    g.drawWithSVGStyle(
                        effectiveStyles,
                        {
                            if(!skipFill) {
                                it.fill(path)
                            }
                        },
                        {it.draw(path)},
                        scaler
                    )
                    path = null
                }


                SVGOperationType.RelativeMove -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeMoveOperation).target.first, it.target.second)
                    if(path == null){
                        path = GeneralPath()
                    }
                    val last = path!!.currentPoint ?: Point(0, 0)
                    path!!.moveTo(mx + last.x, my + last.y)
                }

                SVGOperationType.Line -> {
                    val (mx, my) = coordinateMapper((it as SVGLineOperation).target.first, it.target.second)
                    path!!.lineTo(mx, my)
                }

                SVGOperationType.RelativeLine -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeLineOperation).target.first, it.target.second)
                    val last = path!!.currentPoint
                    path?.lineTo(mx + last.x, my + last.y)
                }

                SVGOperationType.Arc -> {

                    val (mx, my) = coordinateMapper((it as SVGArcOperation).target.first, it.target.second)
                    val radScaledX = scaler.first(it.rad.first)
                    val radScaledY = scaler.second(it.rad.second)
                    val _last = path!!.currentPoint

                    val last = Vector2(_last.x, _last.y)
                    val tgt = Vector2(mx, my)

                    val rotMatrix = Matrix22.getRotationMatrix(-it.xDeg * PI / 180.0)
                    val scaleMatrix = Matrix22.getScaleMatrix(1 / radScaledX, 1 / radScaledY)

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
                    val q = 1 - 0.25 * d * d
                    val sec = when{ //Double precision is sometimes not enough..
                        q >= 0 -> q
                        else -> 0.0
                    }
                    val fac = sqrt(sec)
                    val ellipseCenter = center.plus(orthoUni.scale(fac))

                    val v0 = lastTransformed.minus(ellipseCenter)
                    val v1 = tgtTransformed.minus(ellipseCenter)

                    val angle0 = atan2(v0.y, v0.x)
                    val angle1 = atan2(v1.y, v1.x)

                    val dir01 = angle1 - angle0
                    val circ = 2 * PI
                    val (_base, _step, _end) = when {
                        dir01 >= PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 >= PI -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 && it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1)
                        dir01 >= 0 -> Triple(angle1, 0.1, angle0 + circ)
                        dir01 > -PI && it.flgLongArc == 0 -> Triple(angle1, 0.1, angle0)
                        dir01 > -PI -> Triple(angle0, 0.1, angle1 + circ)
                        it.flgLongArc == 0 -> Triple(angle0, 0.1, angle1 + circ)
                        else -> Triple(angle1, 0.1, angle0)
                    }
                    val (base, step, end) = when(it.flgDir){
                        0 -> Triple(_end, -_step, _base)
                        else -> Triple(_base, _step, _end)
                    }

                    val numPoints = floor((end - base) / step).toInt()
                    val points = (0 until numPoints).map { base + (it + 1) * step }.map { Vector2(cos(it), sin(it)) }
                        .map { it.plus(ellipseCenter) }

                    val rotMatrixInverted = rotMatrix.invert()
                    val scaleMatrixInverted = scaleMatrix.invert()
                    val pointsExtracted = points.map { rotMatrixInverted.mul(scaleMatrixInverted.mul(it)) }

                    pointsExtracted.forEach {
                        path!!.lineTo(it.x, it.y)
                    }
                    path!!.lineTo(tgt.x, tgt.y)
                }      //FIXME, duplication..
                SVGOperationType.RelativeArc -> {
                    val (mx, my) = coordinateMapper((it as SVGRelativeArcOperation).target.first, it.target.second)
                    val radScaledX = scaler.first(it.rad.first)
                    val radScaledY = scaler.second(it.rad.second)

                    val _last = path!!.currentPoint

                    val last = Vector2(_last.x, _last.y)
                    val tgt = last.plus(Vector2(mx, my))

                    val rotMatrix = Matrix22.getRotationMatrix(-it.xDeg * PI / 180.0)
                    val scaleMatrix = Matrix22.getScaleMatrix(1 / radScaledX, 1 / radScaledY)

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
                        path!!.lineTo(it.x, it.y)
                    }
                    path!!.lineTo(tgt.x, tgt.y)
                }

                SVGOperationType.HorizontalLine -> {
                    val prev = path!!.currentPoint
                    val (tgtX, _) = coordinateMapper((it as SVGHorizontalLine).dx, 0.0)
                    path!!.lineTo(tgtX, prev.y)
                }

                SVGOperationType.RelativeHorizontalLine -> {
                    val prev = path!!.currentPoint
                    val (tgtX, _) = coordinateMapper((it as SVGRelativeHorizontalLine).dx, 0.0)
                    path!!.lineTo(prev.x + tgtX, prev.y)
                }

                SVGOperationType.VerticalLine -> {
                    val prev = path!!.currentPoint
                    val (_, tgtY) = coordinateMapper(0.0, (it as SVGVerticalLine).dy)
                    path!!.lineTo(prev.x, tgtY)
                }

                SVGOperationType.RelativeVerticalLine -> {
                    val prev = path!!.currentPoint
                    val (_, tgtY) = coordinateMapper(0.0, (it as SVGRelativeVerticalLine).dy)
                    path!!.lineTo(prev.x, tgtY + prev.y)
                }

                SVGOperationType.RelativeCubicBezier -> {
                    val prev = path!!.currentPoint
                    val cbDesc = it as SVGRelativeCubicBezierOperation
                    val b1 = coordinateMapper(cbDesc.b1.first, cbDesc.b1.second)
                    val b2 = coordinateMapper(cbDesc.b2.first, cbDesc.b2.second)
                    val end = coordinateMapper(cbDesc.end.first, cbDesc.end.second)
                    path!!.curveTo(
                        b1.first + prev.x, b1.second + prev.y,
                        b2.first + prev.x, b2.second + prev.y,
                        end.first + prev.x, end.second + prev.y
                    )
                }

                SVGOperationType.CubicBezier -> {
                    val cbDesc = it as SVGCubicBezierOperation
                    val b1 = coordinateMapper(cbDesc.b1.first, cbDesc.b1.second)
                    val b2 = coordinateMapper(cbDesc.b2.first, cbDesc.b2.second)
                    val end = coordinateMapper(cbDesc.end.first, cbDesc.end.second)
                    path!!.curveTo(
                        b1.first, b1.second,
                        b2.first, b2.second,
                        end.first, end.second
                    )
                }
                SVGOperationType.RelativeSmoothCubicBezier -> {
                    val origin = path!!.currentPoint

                    val b1 = when{
                        lastOp!! is SVGRelativeCubicBezierOperation -> {
                            val b1ProtoRaw = (lastOp!! as SVGRelativeCubicBezierOperation).b2
                            val b1Proto = coordinateMapper(b1ProtoRaw.first, b1ProtoRaw.second)
                            val b2Last = Vector2(b1Proto.first + lastOrigin!!.x, b1Proto.second + lastOrigin!!.y)

                            Vector2(2*origin.x - b2Last.x, 2 * origin.y - b2Last.y)
                        }
                        lastOp!! is SVGCubicBezierOperation -> {
                            val b1ProtoRaw = (lastOp!! as SVGCubicBezierOperation).b2
                            val b1Proto = coordinateMapper(b1ProtoRaw.first, b1ProtoRaw.second)
                            val b2Last = Vector2(b1Proto.first, b1Proto.second)

                            Vector2(2*origin.x - b2Last.x, 2 * origin.y - b2Last.y)
                        }
                        else -> {
                            Vector2(origin.x, origin.y)
                        }
                    }

                    val cbDesc = it as SVGRelativeSmoothCubicBezierOperation
                    val b2 = coordinateMapper(cbDesc.b2.first, cbDesc.b2.second)
                    val end = coordinateMapper(cbDesc.end.first, cbDesc.end.second)

                    path!!.curveTo(
                        b1.x, b1.y,
                        b2.first + origin.x, b2.second + origin.y,
                        end.first + origin.x, end.second + origin.y
                    )
                }
            }
            lastOp = it
            lastOrigin = origin
        }

        path?.let {p ->
            g.drawWithSVGStyle(
                effectiveStyles,
                {
                    if(!skipFill) {
                        it.fill(path)
                    }
                },
                { it.draw(p) },
                scaler
            )
        }

    }
}
