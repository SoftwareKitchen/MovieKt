package tech.softwarekitchen.moviekt.clips.video.util

import java.awt.Graphics2D
import java.awt.geom.Rectangle2D

fun String.getDrawSize(graphics: Graphics2D): Rectangle2D {
    return graphics.font.getStringBounds(this, graphics.fontRenderContext)
}