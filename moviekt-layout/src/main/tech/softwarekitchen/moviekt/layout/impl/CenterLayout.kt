package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.LoggerFactory
import tech.softwarekitchen.moviekt.core.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout

class CenterLayout: Layout() {
    override fun addChild(vararg child: VideoClip) {
        if(readChildren{1}.size + child.size > 1){
            throw Exception()
        }
        super.addChild(*child)
    }
    override fun recalculateChildren() {
        val child = readChildren{it}.firstOrNull() ?: run {
            return
        }
        val size = getSize()
        val halfFreeSpace = size.plus(child.getSize().invert()).scale(0.5)
        child.set(PropertyKey_Position, halfFreeSpace)
    }
}
