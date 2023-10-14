package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.LoggerFactory
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout

class CenterLayout: Layout() {
    override val logger = LoggerFactory.getLogger(javaClass)
    override fun addChild(child: VideoClip) {
        if(getChildren().size >= 1){
            logger.error("Multiple children in CenterLayout are not supported")
            throw Exception()
        }
        super.addChild(child)
    }
    override fun recalculateChildren() {
        val child = getChildren().firstOrNull() ?: run {
            logger.warn("No content in CenterLayout")
            return
        }
        val size = getSize()
        val halfFreeSpace = size.plus(child.getSize().invert()).scale(0.5)
        child.set(PropertyKey_Position, halfFreeSpace)
    }
}
