package tech.softwarekitchen.moviekt.clips.video.image.svg.draw

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.image.svg.CoordinateMapper
import tech.softwarekitchen.moviekt.clips.video.image.svg.Scaler2D
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.SVGItem
import tech.softwarekitchen.moviekt.clips.video.image.svg.model.SVGStyle
import java.awt.Graphics2D

interface SVGPartialDrawer<T: SVGItem> {
    fun draw(it: T, size: Vector2i, target: Graphics2D, coordinateMapper: CoordinateMapper, scaler: Scaler2D, parentStyles: List<SVGStyle>)
}
