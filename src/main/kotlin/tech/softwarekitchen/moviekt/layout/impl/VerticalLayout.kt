package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.layout.Layout
import tech.softwarekitchen.moviekt.util.Padding

data class VerticalLayoutConfiguration(val padding: Padding = Padding(0,0,0,0), val spaceBetween: Int = 0)

class VerticalLayout(private val configuration: VerticalLayoutConfiguration = VerticalLayoutConfiguration()): Layout() {
    companion object{
        fun of(padding: Padding, spaceBetween: Int, vararg clips: VideoClip): VerticalLayout {
            val layout = VerticalLayout(VerticalLayoutConfiguration(padding, spaceBetween))
            clips.forEach(layout::addChild)
            return layout
        }
    }

    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun recalculateChildren(){
        val children = getChildren()

        if(children.isEmpty()){
            logger.debug("No children present")
            return
        }

        logger.debug("Total space {} {} Padding {}", getSize().x, getSize().y, configuration.padding)
        val totalSpace = Vector2i(getSize().x - configuration.padding.left - configuration.padding.right, getSize().y - configuration.padding.top - configuration.padding.bottom - (children.size * configuration.spaceBetween))
        val spacePerChild = Vector2i(totalSpace.x, totalSpace.y / children.size)
        logger.debug("Space per child {}", spacePerChild)
        children.forEachIndexed {
            index, videoClip ->
            videoClip.set(PropertyKey_Position, Vector2i(configuration.padding.left, configuration.padding.top + index * (spacePerChild.y + configuration.spaceBetween)))
            videoClip.set(PropertyKey_Size, spacePerChild)
        }
    }
}
