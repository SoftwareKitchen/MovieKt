package tech.softwarekitchen.moviekt.layout.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.layout.Layout

class OverlayLayout: Layout("OverlayLayout") {
    override fun recalculateChildren() {
        onChildren{
            it.set(PropertyKey_Size, getSize())
            it.set(PropertyKey_Position, Vector2i(0,0))
        }
    }
}
