package tech.softwarekitchen.moviekt.clips.video.image.svg.model

enum class SVGOperationType{
    Move, RelativeMove, CubicBezier, RelativeCubicBezier, RelativeSmoothCubicBezier, Line, RelativeLine, Arc, RelativeArc, HorizontalLine, RelativeHorizontalLine, VerticalLine, RelativeVerticalLine, ClosePath
}

interface SVGOperation{
    val type: SVGOperationType
}

class SVGLineOperation(val target: Pair<Double, Double>): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.Line
}
class SVGRelativeLineOperation(val target: Pair<Double, Double>): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeLine
}
class SVGRelativeSmoothCubicBezierOperation(val b2: Pair<Double, Double>, val end: Pair<Double, Double>):
    SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeSmoothCubicBezier
}

class SVGCubicBezierOperation(val b1: Pair<Double, Double>, val b2: Pair<Double, Double>, val end: Pair<Double, Double>):
    SVGOperation {
    override val type: SVGOperationType = SVGOperationType.CubicBezier
}
class SVGRelativeCubicBezierOperation(val b1: Pair<Double, Double>, val b2: Pair<Double, Double>, val end: Pair<Double, Double>):
    SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeCubicBezier
}
class SVGMoveOperation(val target: Pair<Double, Double>): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.Move
}
class SVGRelativeMoveOperation(val target: Pair<Double, Double>): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeMove
}
class SVGArcOperation(val rad: Pair<Double, Double>, val xDeg: Double, val flgLongArc: Int, val flgDir: Int, val target: Pair<Double, Double>):
    SVGOperation {
    override val type: SVGOperationType = SVGOperationType.Arc
}
class SVGRelativeArcOperation(val rad: Pair<Double, Double>, val xDeg: Double, val flgLongArc: Int, val flgDir: Int, val target: Pair<Double, Double>):
    SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeArc
}
class SVGHorizontalLine(val dx: Double): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.HorizontalLine
}
class SVGRelativeHorizontalLine(val dx: Double): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeHorizontalLine
}

class SVGVerticalLine(val dy: Double): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.VerticalLine
}

class SVGRelativeVerticalLine(val dy: Double): SVGOperation {
    override val type: SVGOperationType = SVGOperationType.RelativeVerticalLine
}

class SVGClosePath: SVGOperation {
    override val type: SVGOperationType = SVGOperationType.ClosePath
}
