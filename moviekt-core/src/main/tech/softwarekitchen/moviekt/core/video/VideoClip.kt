package tech.softwarekitchen.moviekt.core.video

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.core.exception.UnknownPropertyException
import tech.softwarekitchen.moviekt.core.extension.MovieKtVideoExtensionContainer
import tech.softwarekitchen.moviekt.core.mutation.MovieKtMutation
import tech.softwarekitchen.moviekt.core.util.VideoTimestamp
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_Variant
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.roundToInt

abstract class VideoClip(
    val id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val volatile: Boolean = false,
    timeShift: Float = 0f
) {
    companion object{
        val PropertyKey_Offset = "Offset"
        val PropertyKey_Opacity = "Opacity"
        val PropertyKey_Position = "Position"
        val PropertyKey_Size = "Size"
        val PropertyKey_TimeShift = "TimeShift"
        val PropertyKey_Visible = "Visible"


    }
    protected class ActiveMutation(val onTick: (Float) -> Unit, val onClose: () -> Unit)

    open class VideoClipProperty<T>(val name: String, private val initialValue: T, private val onChange: (T) -> Unit, private val converter: (Any) -> T = {it as T}){
        private var value: T = initialValue
        val v: T
            get(){return value}
        fun setDirect(n: T){
            if(value == n){
                return
            }
            value = n
            onChange(n)
        }
        fun set(nv: Any){
            val x = converter(nv)
            if(x == value){
                return
            }
            value = x
            onChange(nv as T)
        }

        fun reset(){
            setDirect(initialValue)
        }
    }

    private val children =  ArrayList<VideoClip>()

    fun <T: Any>readChildren(op: (VideoClip) -> T): List<T>{
        return children.map(op)
    }

    private val offsetProperty = VideoClipProperty(PropertyKey_Offset, Vector2i(0,0), this::markPseudoDirty)
    private val opacityProperty = VideoClipProperty(PropertyKey_Opacity, 1f, this::markOpacityChanged)
    private val positionProperty = VideoClipProperty(PropertyKey_Position, position, this::markPseudoDirty)
    private val sizeProperty = VideoClipProperty(PropertyKey_Size, size,{onResize(); markDirty(null)})
    private val visibleProperty = VideoClipProperty(PropertyKey_Visible, visible, this::markVisibilityChanged)
    private val timeShiftProperty = VideoClipProperty(PropertyKey_TimeShift, timeShift, this::markDirty)
    private val properties: MutableList<VideoClipProperty<*>> = arrayListOf(
        offsetProperty,
        opacityProperty,
        positionProperty,
        sizeProperty,
        visibleProperty,
        timeShiftProperty
    )
    val extensions = ArrayList<MovieKtVideoExtensionContainer>()

    fun <T>readProperties(op: (VideoClipProperty<*>) -> T): List<T>{
        return properties.map(op)
    }

    protected fun registerProperty(vararg property: VideoClipProperty<*>){
        property.forEach{
            properties.add(it)
        }
    }

    abstract fun renderContent(img: BufferedImage, t: VideoTimestamp)

    private val addChildListeners = ArrayList<(VideoClip) -> Unit>()
    fun addAddChildListeners(listener: (VideoClip) -> Unit){
        addChildListeners.add(listener)
    }
    open fun addChild(vararg child: VideoClip){
        child.forEach(children::add)
        addChildListeners.forEach{acl -> child.forEach(acl)}
        markDirty(null)
    }

    private val removeChildListeners = ArrayList<(VideoClip) -> Unit>()
    fun addRemoveChildListener(listener: (VideoClip) -> Unit){
        removeChildListeners.add(listener)
    }
    open fun removeChild(child: VideoClip){
        children.remove(child)
        removeChildListeners.forEach{
            it(child)
        }
    }

    fun onChildren(op: (VideoClip) -> Unit){
        children.forEach(op)
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

    fun getTimeShift(): Float{
        return timeShiftProperty.v
    }

    private lateinit var cache: BufferedImage
    private var cacheDirty = true
    private var pseudoDirty = false
    private var opacityChanged = false
    private var visibilityChanged = false
    protected fun markDirty(ignored: Any?){
        cacheDirty = true
    }
    protected fun markVisibilityChanged(ignored: Any?){
        visibilityChanged = true
    }
    protected fun markPseudoDirty(ignored: Any?){
        pseudoDirty = true
    }

    protected fun markOpacityChanged(ignored: Any?){
        opacityChanged = true
    }

    fun hasOpacityChanged(): Boolean{
        return opacityChanged
    }
    fun hasVisibilityChanged(): Boolean{
        return visibilityChanged
    }

    fun needsRepaint(): Boolean{
        return cacheDirty || pseudoDirty || opacityChanged || children.any{it.needsRepaint()} || volatile || visibilityChanged
    }
    fun clearRepaintFlags(){
        cacheDirty = false
        pseudoDirty = false
        opacityChanged = false
        visibilityChanged = false
    }

    fun findById(id: String): List<VideoClip>{
        return when(this.id == id){
            true -> children.map{it.findById(id)}.flatten() + this
            false -> children.map{it.findById(id)}.flatten()
        }
    }

    fun set(key: String, value: Any){
        val prop = properties.firstOrNull{it.name == key} ?: throw UnknownPropertyException(key, this.id)
        prop.set(value)
    }

    fun has(key: String): Boolean{
        return properties.any{it.name == key}
    }

    fun reset(key: String){
        properties.firstOrNull{it.name == key}?.reset()
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
                        },
                        {
                            set(PropertyKey_Size, Vector2i(targetWidth, targetHeight))
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

    fun getOpacity(): Float{
        return opacityProperty.v
    }

    inline fun <reified T: MovieKtVideoExtensionContainer>getExtensionContainer(creator: (VideoClip) -> T): T {
        extensions.firstOrNull{it is T}?.let{ return it as T}
        val created = creator(this)
        extensions.add(created)
        return created
    }
}
