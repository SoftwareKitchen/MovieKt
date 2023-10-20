package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout
import tech.softwarekitchen.moviekt.util.Padding

data class HorizontalLayoutConfiguration(val padding: Padding = Padding(0,0,0,0), val spaceBetween: Int = 0)

class HorizontalLayout(private val configuration: HorizontalLayoutConfiguration = HorizontalLayoutConfiguration()): Layout() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun recalculateChildren(){
        val children = getChildren()

        logger.debug("Total space {} {} Padding {}", getSize().x, getSize().y, configuration.padding)
        val totalSpace = Vector2i(getSize().x - configuration.padding.left - configuration.padding.right, getSize().y - configuration.padding.top - configuration.padding.bottom - (children.size * configuration.spaceBetween))
        val spacePerChild = Vector2i(totalSpace.x / children.size, totalSpace.y)
        logger.debug("Space per child {}", spacePerChild)
        children.forEachIndexed {
            index, videoClip ->
            videoClip.set(PropertyKey_Position, Vector2i(configuration.padding.left + index * (spacePerChild.x + configuration.spaceBetween), configuration.padding.top))
            videoClip.set(PropertyKey_Size, spacePerChild)
        }
    }
}
