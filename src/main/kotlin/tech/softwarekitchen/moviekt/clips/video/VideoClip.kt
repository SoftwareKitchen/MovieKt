package tech.softwarekitchen.moviekt.clips.video

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.mutation.MovieKtMutation
import java.awt.image.BufferedImage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

abstract class VideoClip(val id: String, size: Vector2i, position: Vector2i, visible: Boolean){
    companion object{
        val PropertyKey_Offset = "Offset"
        val PropertyKey_Opacity = "Opacity"
        val PropertyKey_Position = "Position"
        val PropertyKey_Size = "Size"
        val PropertyKey_Visible = "Visible"
    }
    protected class ActiveMutation(val onTick: (Float) -> Unit, val onClose: () -> Unit)

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

    private val addChildListeners = ArrayList<(VideoClip) -> Unit>()
    fun addAddChildListeners(listener: (VideoClip) -> Unit){
        addChildListeners.add(listener)
    }
    fun addChild(child: VideoClip){
        children.add(child)

        addChildListeners.forEach{
            it(child)m ,mn
        }
    }

    private val removeChildListeners = ArrayList<(VideoClip) -> Unit>()
    fun addRemoveChildListener(listener: (VideoClip) -> Unit){
        removeChildListeners.add(listener)
    }
    fun removeChild(child: VideoClip){
        children.remove(child)
        removeChildListeners.forEach{
            it(child)
        }
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

    private val mutations = HashMap<String, (MovieKtMutation) -> Pair<String, ActiveMutation>>()
    private val activeMutations = HashMap<String, ActiveMutation>()

    protected fun registerMutation(name: String, callback: (MovieKtMutation) -> Pair<String, ActiveMutation>){
        mutations[name] = callback
    }

    protected fun registerActiveMutation(id: String, callbacks: ActiveMutation){
        activeMutations[id] = callbacks
    }

    fun prepareMutation(mutation: MovieKtMutation): String{
        return when(mutation.type){
            "move" -> {
                val targetBase = mutation.base["target"] as Map<String, Any>
                val targetX = targetBase["x"] as Int
                val targetY = targetBase["y"] as Int
                val id = UUID.randomUUID().toString()

                val src = getPosition()
                registerActiveMutation(
                    id,
                    ActiveMutation(
                        {
                            val loc = Vector2i((src.x * (1f - it) + targetX * it).roundToInt(), (src.y * (1f - it) + targetY * it).roundToInt())
                            set(PropertyKey_Position, loc)
                            onMove()
                        },
                        {
                            set(PropertyKey_Position, Vector2i(targetX, targetY))
                            onMove()
                        }
                    )
                )
                id
            }
            "resize" -> {
                val sizeBase = mutation.base["target"] as Map<String, Any>
                val targetWidth = sizeBase["width"] as Int
                val targetHeight = sizeBase["height"] as Int
                val id = UUID.randomUUID().toString()

                val src = getSize()
                registerActiveMutation(
                    id,
                    ActiveMutation(
                        {
                            val size = Vector2i((src.x * (1f - it) + targetWidth * it).roundToInt(), (src.y * (1f - it) + targetHeight * it).roundToInt())
                            set(PropertyKey_Size, size)
                            onResize()
                        },
                        {
                            set(PropertyKey_Size, Vector2i(targetWidth, targetHeight))
                            onResize()
                        }
                    )
                )
                id
            }
            else -> {
                val cb = mutations[mutation.type] ?: throw Exception()
                val res = cb(mutation)
                activeMutations[res.first] = res.second
                res.first
            }
        }
    }

    fun applyKeyframe(mutation: String, value: Float){
        activeMutations[mutation]!!.onTick(value)
    }
    fun removeMutation(id: String){
        activeMutations[id]!!.onClose()
        activeMutations.remove(id)
    }

    open fun onResize(){}
    open fun onMove(){}
}
