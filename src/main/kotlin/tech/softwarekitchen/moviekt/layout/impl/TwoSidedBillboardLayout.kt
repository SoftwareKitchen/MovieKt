package tech.softwarekitchen.moviekt.layout.impl

import tech.softwarekitchen.common.vector.Vector2
import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.common.vector.Vector3
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.layout.Layout
import tech.softwarekitchen.tsr.camera.Camera
import tech.softwarekitchen.tsr.color.Color
import tech.softwarekitchen.tsr.light.AmbientLight
import tech.softwarekitchen.tsr.`object`.TextureSection
import tech.softwarekitchen.tsr.`object`.TexturedTriangle
import tech.softwarekitchen.tsr.scene.Scene
import java.awt.image.BufferedImage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class TwoSidedBillboardLayout(name: String): Layout(name){
    private val children = ArrayList<VideoClip>()
    companion object{
        val PropertyKey_BillboardState = "BBState"
        val PropertyKey_Backshift = "Backshift"
    }

    private val bbStateProperty = VideoClipProperty(PropertyKey_BillboardState, 0.0, this::markDirty)
    private val backshiftProperty = VideoClipProperty(PropertyKey_Backshift, 0.15, this::markDirty)

    init{
        registerProperty(bbStateProperty, backshiftProperty)
    }
    override fun recalculateChildren() {
        children.forEach{
            it.set(PropertyKey_Position, Vector2i(0,0))
            it.set(PropertyKey_Size, getSize())
        }
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val g = img.createGraphics()
        val bbState = bbStateProperty.v
        if(bbState <= 0.0){
            g.drawImage(getPrimaryImage(t),0,0,null)
            return
        }
        if(bbState >= 1.0){
            g.drawImage(getSecondaryImage(t),0,0,null)
            return
        }

        val img = renderAnimationFrame(getPrimaryImage(t), getSecondaryImage(t), bbState)
        g.drawImage(img, 0, 0, null)
    }

    private fun getPrimaryImage(t: VideoTimestamp): BufferedImage{
        val i0 = BufferedImage(getSize().x, getSize().y, BufferedImage.TYPE_INT_ARGB)
        if(children.isNotEmpty()){
            children[0].renderContent(i0, t)
        }
        return i0
    }

    private fun getSecondaryImage(t: VideoTimestamp): BufferedImage{
        val i0 = BufferedImage(getSize().x, getSize().y, BufferedImage.TYPE_INT_ARGB)
        if(children.size > 1){
            children[1].renderContent(i0, t)
        }
        return i0
    }

    override fun addChild(child: VideoClip) {
        if(children.size >= 2){
            println("WARNING: Adding more than two children on a TwoSidedBillboardLayout will have them being ignored")
        }
        children.add(child)
        recalculateChildren()
    }

    override fun removeChild(child: VideoClip) {
        children.remove(child)
    }

    private fun renderAnimationFrame(i0: BufferedImage, i1: BufferedImage, ratio: Double): BufferedImage{
        val wid = i0.width
        val hei = i0.height
        val fov = 3.0
        val imgSeparation = 0.01
        val fovRad = fov * PI / 180.0
        val shiftBack = 1 + backshiftProperty.v * sin(PI * ratio)
        val dist = when{
            wid > hei ->{
                wid / (2 * tan(fovRad / 2))
            }
            else -> {
                hei / (2 * tan(fovRad / 2))
            }
        } * shiftBack

        val camera = Camera(
            Vector3(dist,0.0,0.0),
            Vector3(-1.0,0.0,0.0),
            Vector3(0.0,1.0,0.0),
            fov,
            Vector2i(wid, hei),
            Color.fromARGB(0u),
            listOf(AmbientLight(1.0))
        )

        val effAngleRatio = (1 - cos(PI * ratio)) / 2.0

        val img1BottomLeft = Vector3(imgSeparation, -hei / 2.0,-wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img1BottomRight = Vector3(imgSeparation, -hei / 2.0,wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img1TopLeft = Vector3(imgSeparation, hei / 2.0,-wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img1TopRight = Vector3(imgSeparation, hei / 2.0,wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img2BottomLeft = Vector3(-imgSeparation, -hei / 2.0,-wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img2BottomRight = Vector3(-imgSeparation, -hei / 2.0,wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img2TopLeft = Vector3(-imgSeparation, hei / 2.0,-wid/2.0).rotateAroundYAxis(PI * effAngleRatio)
        val img2TopRight = Vector3(-imgSeparation, hei / 2.0,wid/2.0).rotateAroundYAxis(PI * effAngleRatio)

        val texBottomLeft = Vector2(0.0,1.0)
        val texBottomRight = Vector2(1.0,1.0)
        val texTopLeft = Vector2(0.0,0.0)
        val texTopRight = Vector2(1.0,0.0)

        val img1Tri1 = TexturedTriangle(
            img1BottomLeft, img1TopLeft, img1BottomRight, TextureSection(i0,texBottomLeft, texTopLeft, texBottomRight)
        )
        val img1Tri2 = TexturedTriangle(
            img1TopLeft, img1TopRight, img1BottomRight, TextureSection(i0, texTopLeft, texTopRight, texBottomRight)
        )

        val img2Tri1 = TexturedTriangle(
            img2BottomLeft, img2TopLeft, img2BottomRight, TextureSection(i1,texBottomRight, texTopRight, texBottomLeft)
        )
        val img2Tri2 = TexturedTriangle(
            img2TopLeft, img2TopRight, img2BottomRight, TextureSection(i1, texTopRight, texTopLeft, texBottomLeft)
        )

        val scene = Scene(listOf(img1Tri1, img1Tri2, img2Tri1, img2Tri2))
        return camera.render(scene)
    }
}

private fun Vector3.rotateAroundYAxis(amt: Double): Vector3{
    return Vector3(cos(amt) * x + sin(amt) * z,y, cos(amt) * z - sin(amt) * x)
}
