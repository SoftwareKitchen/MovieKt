package tech.softwarekitchen.moviekt.clips.video

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.animation.MovieKtAnimation
import tech.softwarekitchen.moviekt.clips.audio.AudioClip
import tech.softwarekitchen.moviekt.clips.audio.basic.AudioContainerClip
import tech.softwarekitchen.moviekt.exception.UnknownPropertyException
import tech.softwarekitchen.moviekt.filter.VideoClipFilter
import tech.softwarekitchen.moviekt.filter.VideoClipFilterChain
import tech.softwarekitchen.moviekt.mutation.MovieKtMutation
import tech.softwarekitchen.moviekt.theme.ThemedClip
import tech.softwarekitchen.moviekt.theme.VideoTheme.Companion.VTPropertyKey_Variant
import tech.softwarekitchen.moviekt.util.Pixel
import java.awt.image.BufferedImage
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class VideoTimestamp(val t: Double, val frame: Int, val totFrames: Int)

abstract class VideoClip(
    val id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val volatile: Boolean = false,
    timeShift: Float = 0f
): AudioClip(1), ThemedClip{
    companion object{
        val PropertyKey_Offset = "Offset"
        val PropertyKey_Opacity = "Opacity"
        val PropertyKey_Position = "Position"
        val PropertyKey_Size = "Size"
        val PropertyKey_TimeShift = "TimeShift"
        val PropertyKey_Visible = "Visible"
    }
    protected class ActiveMutation(val onTick: (Float) -> Unit, val onClose: () -> Unit)

    protected open class VideoClipProperty<T>(val name: String, initialValue: T, private val onChange: (T) -> Unit, private val converter: (Any) -> T = {it as T}){
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
    }

    private val children =  ArrayList<VideoClip>()

    private val offsetProperty = VideoClipProperty(PropertyKey_Offset, Vector2i(0,0), this::markPseudoDirty)
    private val opacityProperty = VideoClipProperty(PropertyKey_Opacity, 1f, this::markOpacityChanged)
    private val positionProperty = VideoClipProperty(PropertyKey_Position, position, this::markPseudoDirty)
    private val sizeProperty = VideoClipProperty(PropertyKey_Size, size,{onResize(); markDirty(null)})
    private val visibleProperty = VideoClipProperty(PropertyKey_Visible, visible, this::markVisibilityChanged)
    private val timeShiftProperty = VideoClipProperty(PropertyKey_TimeShift, timeShift, this::markDirty)
    private val variantProperty = VideoClipProperty<String?>(VTPropertyKey_Variant, null, this::markDirty)
    private val properties: MutableList<VideoClipProperty<*>> = arrayListOf(
        offsetProperty,
        opacityProperty,
        positionProperty,
        sizeProperty,
        visibleProperty,
        variantProperty,
        timeShiftProperty
    )

    override fun getVariant(): String? {
        return variantProperty.v
    }

    fun setVariant(variant: String?){
        variantProperty.setDirect(variant)
    }

    protected fun registerProperty(vararg property: VideoClipProperty<*>){
        property.forEach{
            properties.add(it)
        }
    }

    protected class VideoClipThemeProperty<T>(name: String, initialValue: T, onChange: (T) -> Unit, converter: (Any) -> T = {it as T}): VideoClipProperty<T>(name, initialValue, onChange, converter){

    }
    protected fun registerThemedProperty(vararg tProperties: VideoClipThemeProperty<*>){
        tProperties.forEach(properties::add)
    }

    override fun getPossibleThemeProperties(): List<String>{
        return properties.filter{it is VideoClipThemeProperty<*>}.map{it.name}
    }

    abstract fun renderContent(img: BufferedImage, t: VideoTimestamp)

    private val addChildListeners = ArrayList<(VideoClip) -> Unit>()
    fun addAddChildListeners(listener: (VideoClip) -> Unit){
        addChildListeners.add(listener)
    }
    open fun addChild(vararg child: VideoClip){
        child.forEach(children::add)
        addChildListeners.forEach{acl -> child.forEach(acl)}
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

    override fun getChildren(): List<VideoClip>{
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

    override fun set(key: String, value: Any){
        val prop = properties.firstOrNull{it.name == key} ?: throw UnknownPropertyException(key, this.id)
        prop.set(value)
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

    private val filterChain = VideoClipFilterChain()
    fun addFilter(filter: VideoClipFilter){
        filterChain.addFilter(filter)
    }

    fun runThroughFilter(x: Int, y: Int, xSize: Int, ySize: Int, pixel: Pixel): Pixel{
        return filterChain.filter(x, y, xSize, ySize, pixel)
    }

    fun getOpacity(): Float{
        return opacityProperty.v
    }

    private val rawAnimations = ArrayList<MovieKtAnimation<*>>()
    private val rawMutations = ArrayList<MovieKtMutation>()

    fun addRawAnimation(anim: MovieKtAnimation<*>){
        rawAnimations.add(anim)
    }

    fun addRawMutation(mut: MovieKtMutation){
        rawMutations.add(mut)
    }

    fun getAnimations(): List<MovieKtAnimation<*>>{
        return (children.map{it.getAnimations()}.flatten() + rawAnimations).map{it.shift(timeShiftProperty.v)}
    }

    fun getMutations(): List<MovieKtMutation>{
        return (children.map{it.getMutations()}.flatten() + rawMutations).map{it.shift(timeShiftProperty.v)}
    }

    private val audioChildren = AudioContainerClip(1)
    fun addAudioChild(audioClip: AudioClip, offset: Double){
        audioChildren.addClip(audioClip, offset)
    }
    override fun getAt(t: Double): List<Double> {
        val audioSum = ArrayList<Double>()
        for(i in 0 until numChannels){
            audioSum.add(0.0)
        }
        children.forEach{
            val audio = it.getAt(t - it.timeShiftProperty.v)
            audio.forEachIndexed {
                i, v ->
                audioSum[i] += v
            }
        }
        audioChildren.getAt(t).forEachIndexed {
            i, v ->
            audioSum[i] += v
        }
        return audioSum.map{min(1.0,max(it, -1.0))}
    }
}
