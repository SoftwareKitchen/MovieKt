package tech.softwarekitchen.moviekt.clips.video.image.svg.draw

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.CoordinateMapper
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import tech.softwarekitchen.moviekt.clips.video.image.svg.mergeStyles
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.*
import java.awt.Graphics2D

class SVGPathDrawer: SVGPartialDrawer<SVGPath> {
    private val simple = SVGSimplePathDrawer()
    private val complex = SVGComplexPathDrawer()
    override fun draw(
        it: SVGPath,
        size: Vector2i,
        target: Graphics2D,
        coordinateMapper: CoordinateMapper,
        scaler: Scaler2D,
        parentStyles: List<SVGStyle>
    ) {
        val styles = mergeStyles(parentStyles, it.styles)
        val fillStmt = styles.firstOrNull{it.type == SVGStyleType.Fill}
        val hasFill = fillStmt?.let{parseFillColor(it.value)} != null

        val hasEvenOdd = styles.firstOrNull{it.type == SVGStyleType.FillRule}?.let{it.value == "evenodd"} ?: false

        val isComplex = it.operations.any{it.type == SVGOperationType.ClosePath} && it.operations.indexOfFirst{it.type == SVGOperationType.ClosePath} < it.operations.size - 1

        if(hasFill && hasEvenOdd && isComplex){
            complex.draw(it, size ,target, coordinateMapper, scaler, parentStyles)
        }else{
            simple.draw(it, size, target, coordinateMapper, scaler, parentStyles)
        }
    }
}

