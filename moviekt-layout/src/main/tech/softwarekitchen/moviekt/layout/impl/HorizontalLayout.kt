package tech.softwarekitchen.moviekt.layout.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.util.Padding
import tech.softwarekitchen.moviekt.layout.Layout

data class HorizontalLayoutConfiguration(val padding: Padding = Padding(0,0,0,0), val spaceBetween: Int = 0)

class HorizontalLayout(private val configuration: HorizontalLayoutConfiguration = HorizontalLayoutConfiguration()): Layout() {
    override fun recalculateChildren(){
        val children = readChildren{it}

        val totalSpace = Vector2i(getSize().x - configuration.padding.left - configuration.padding.right, getSize().y - configuration.padding.top - configuration.padding.bottom - (children.size * configuration.spaceBetween))
        val spacePerChild = Vector2i(totalSpace.x / children.size, totalSpace.y)
        children.forEachIndexed {
            index, videoClip ->
            videoClip.set(PropertyKey_Position, Vector2i(configuration.padding.left + index * (spacePerChild.x + configuration.spaceBetween), configuration.padding.top))
            videoClip.set(PropertyKey_Size, spacePerChild)
        }
    }
}
