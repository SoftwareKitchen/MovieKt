package tech.softwarekitchen.moviekt.layout

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.basic.ContainerVideoClip
import tech.softwarekitchen.moviekt.core.video.VideoClip

abstract class Layout(name: String = "layout", volatile: Boolean = false): ContainerVideoClip(name,Vector2i(100,100), Vector2i(0,0),true, volatile = volatile) {
    override fun addChild(vararg child: VideoClip){
        super.addChild(*child)
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

    abstract fun recalculateChildren()

    fun fill(parent: VideoClip){
        set(PropertyKey_Position, Vector2i(0,0))
        set(PropertyKey_Size, parent.getSize())
        parent.addChild(this)
    }

}

fun VideoClip.layout(layout: Layout){
    layout.fill(this)
}

fun VideoClip.initializeLayouts(){
    if (this is Layout) {
        this.recalculateChildren()
        getChildren().forEach(VideoClip::initializeLayouts)
    } else {
        getChildren().forEach {
            if (it is Layout) {
                it.set(VideoClip.PropertyKey_Position, Vector2i(0, 0))
                it.set(VideoClip.PropertyKey_Size, this.getSize())
            }
            it.initializeLayouts()
        }
    }
}


