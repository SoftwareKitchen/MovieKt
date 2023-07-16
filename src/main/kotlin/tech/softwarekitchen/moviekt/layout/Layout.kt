package tech.softwarekitchen.moviekt.layout

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.basic.ContainerVideoClip

abstract class Layout(name: String = "layout"): ContainerVideoClip(name,Vector2i(100,100), Vector2i(0,0),true) {
    override fun addChild(child: VideoClip){
        super.addChild(child)
        recalculateChildren()
    }

    override fun removeChild(child: VideoClip) {
        super.removeChild(child)
        recalculateChildren()
    }

    override fun onResize() {
        recalculateChildren()
        super.onResize()
    }

    override fun onMove() {
        recalculateChildren()
        super.onMove()
    }

    protected abstract fun recalculateChildren()

    fun fill(parent: VideoClip){
        set(PropertyKey_Position, Vector2i(0,0))
        set(PropertyKey_Size, parent.getSize())
        parent.addChild(this)
    }

}

fun VideoClip.layout(layout: Layout){
    layout.fill(this)
}
