package tech.softwarekitchen.moviekt.clips.video

import tech.softwarekitchen.common.vector.Vector2i
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.lang.Float.max
import java.lang.Float.min


abstract class VideoClip(val id: String, size: Vector2i, position: Vector2i){
    companion object{
        val PropertyKey_Offset = "Offset"
        val PropertyKey_Opacity = "Opacity"
        val PropertyKey_Position = "Position"
        val PropertyKey_Size = "Size"
        val PropertyKey_Visible = "Visible"
    }
    class VideoClipProperty<T>(val name: String, initialValue: T, private val onChange: () -> Unit){
        private var value: T = initialValue
        val v: T
            get(){return value}
        fun set(nv: Any){
            value = nv as T
            onChange()
        }
    }

    private val children =  ArrayList<VideoClip>()

    private val offsetProperty = VideoClipProperty(PropertyKey_Offset, Vector2i(0,0), this::markPseudoDirty)
    private val opacityProperty = VideoClipProperty(PropertyKey_Opacity, 1f, this::markDirty)
    private val positionProperty = VideoClipProperty(PropertyKey_Position, position, this::markPseudoDirty)
    private val sizeProperty = VideoClipProperty(PropertyKey_Size, size, this::markDirty)
    private val visibleProperty = VideoClipProperty(PropertyKey_Visible, true, this::markPseudoDirty)
    private val properties: MutableList<VideoClipProperty<*>> = arrayListOf(
        offsetProperty,
        opacityProperty,
        positionProperty,
        sizeProperty,
        visibleProperty
    )

    protected fun registerProperty(property: VideoClipProperty<*>){
        properties.add(property)
    }

    fun getProperty(id: String): VideoClipProperty<*>{
        return properties.first{it.name == id}
    }

    abstract fun renderContent(img: BufferedImage)

    fun addChild(child: VideoClip){
        children.add(child)
    }

    fun getPosition(): Vector2i{
        return positionProperty.v.plus(offsetProperty.v)
    }

    fun getSize(): Vector2i{
        return sizeProperty.v
    }

    fun isVisible(): Boolean{
        return visibleProperty.v
    }

    private fun render(): BufferedImage{
        val size = sizeProperty.v
        val content = BufferedImage(size.x, size.y, TYPE_INT_ARGB)
        renderContent(content)

        val g = content.createGraphics()
        children.filter{it.isVisible()}.forEach{
            val img = it.get()
            val pos = it.getPosition()
            g.drawImage(img, pos.x, pos.y, null)
        }

        if(opacityProperty.v < 1f) {
            for (x in 0 until content.width) {
                for (y in 0 until content.height) {
                    val argb = content.getRGB(x, y).toUInt()
                    val rgb = argb % 16777216u
                    val a = argb / 16777216u
                    val aCorrected = min(max(a.toFloat() * opacityProperty.v, 0f), 255f).toUInt()
                    val argbCorrected = aCorrected * 16777216u + rgb
                    content.setRGB(x,y,argbCorrected.toInt())
                }
            }
        }

        return content
    }

    private lateinit var cache: BufferedImage
    private var cacheDirty = true
    private var pseudoDirty = false
    protected fun markDirty(){
        cacheDirty = true
    }
    protected fun markPseudoDirty(){
        pseudoDirty = true
    }
    fun get(): BufferedImage{
        pseudoDirty = false
        if(!needsRepaint()){
            return cache
        }
        cache = render()
        cacheDirty = false
        return cache
    }

    fun needsRepaint(): Boolean{
        return cacheDirty || pseudoDirty || children.any{it.needsRepaint()}
    }

    fun findById(id: String): List<VideoClip>{
        return when(this.id == id){
            true -> children.map{it.findById(id)}.flatten() + this
            false -> children.map{it.findById(id)}.flatten()
        }
    }

    fun set(id: String, value: Any){
        properties.first{it.name == id}.set(value)
    }

    /*protected fun cloneImage(src: BufferedImage): BufferedImage{
        return BufferedImage(src.colorModel, src.copyData(null),src.isAlphaPremultiplied,null)
    }*/
}
