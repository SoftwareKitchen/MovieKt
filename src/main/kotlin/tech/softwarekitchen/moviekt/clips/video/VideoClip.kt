package tech.softwarekitchen.moviekt.clips.video

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.mutation.MovieKtMutation
import java.awt.image.BufferedImage

abstract class VideoClip(val id: String, size: Vector2i, position: Vector2i, visible: Boolean){
    companion object{
        val PropertyKey_Offset = "Offset"
        val PropertyKey_Opacity = "Opacity"
        val PropertyKey_Position = "Position"
        val PropertyKey_Size = "Size"
        val PropertyKey_Visible = "Visible"
    }
    class VideoClipProperty<T>(val name: String, initialValue: T, private val onChange: () -> Unit, private val converter: (Any) -> T = {it as T}){
        private var value: T = initialValue
        val v: T
            get(){return value}
        fun set(nv: Any){
            val x = converter(nv)
            if(x == value){
                return
            }
            value = x
            onChange()
        }
    }

    private val children =  ArrayList<VideoClip>()

    private val offsetProperty = VideoClipProperty(PropertyKey_Offset, Vector2i(0,0), this::markPseudoDirty)
    private val opacityProperty = VideoClipProperty(PropertyKey_Opacity, 1f, this::markOpacityChanged)
    private val positionProperty = VideoClipProperty(PropertyKey_Position, position, this::markPseudoDirty)
    private val sizeProperty = VideoClipProperty(PropertyKey_Size, size, this::markDirty)
    private val visibleProperty = VideoClipProperty(PropertyKey_Visible, visible, this::markOpacityChanged)
    private val properties: MutableList<VideoClipProperty<*>> = arrayListOf(
        offsetProperty,
        opacityProperty,
        positionProperty,
        sizeProperty,
        visibleProperty
    )

    protected fun registerProperty(vararg property: VideoClipProperty<*>){
        property.forEach{
            properties.add(it)
        }
    }

    fun getProperty(id: String): VideoClipProperty<*>{
        return properties.first{it.name == id}
    }

    abstract fun renderContent(img: BufferedImage)

    fun addChild(child: VideoClip){
        children.add(child)
    }

    fun getChildren(): List<VideoClip>{
        return children
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

    private lateinit var cache: BufferedImage
    private var cacheDirty = true
    private var pseudoDirty = false
    private var opacityChanged = false
    protected fun markDirty(){
        cacheDirty = true
    }
    protected fun markPseudoDirty(){
        pseudoDirty = true
    }

    protected fun markOpacityChanged(){
        opacityChanged = true
    }

    fun hasOpacityChanged(): Boolean{
        return opacityChanged
    }

    fun needsRepaint(): Boolean{
        return cacheDirty || pseudoDirty || opacityChanged || children.any{it.needsRepaint()}
    }
    fun clearRepaintFlags(){
        cacheDirty = false
        pseudoDirty = false
        opacityChanged = false
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

    open fun prepareMutation(mutation: MovieKtMutation): String{
        throw Exception()
    }

    open fun applyKeyframe(mutation: String, value: Float){}
    open fun removeMutation(id: String){}
}
