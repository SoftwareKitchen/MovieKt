package tech.softwarekitchen.moviekt.clips.video.text

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import kotlin.math.max

class StaticFormulaVideoClipConfiguration(
    val baseFontSize: Int = 36,
    val fontSizeDecreasePerLevel: Int = 4
)

abstract class FormulaElement{
    abstract fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int)
    abstract fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i
}

class FormulaTextElement(private val text: String): FormulaElement(){
    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        graphics.font = graphics.font.deriveFont(fontsize.toFloat())
        val rect = graphics.font.getStringBounds(text, graphics.fontRenderContext)
        return Vector2i(rect.width.toInt()+4, rect.height.toInt()+4)
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        val size = getRequiredSpace(graphics, fontsize, fontsizeDecrease)
        val bottomAnchor = lmAnchor.y + size.y / 3
        graphics.font = graphics.font.deriveFont(fontsize.toFloat())
        graphics.drawString(text, lmAnchor.x, bottomAnchor)
    }
}

class FormulaFractionElement(private val divided: FormulaElement, private val divisor: FormulaElement): FormulaElement(){
    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        val top = divided.getRequiredSpace(graphics, fontsize - fontsizeDecrease, fontsizeDecrease)
        val bot = divisor.getRequiredSpace(graphics, fontsize - fontsizeDecrease, fontsizeDecrease)
        val wid = max(top.x, bot.x) + 6
        val hei = 2*max(top.y, bot.y) + 7
        return Vector2i(wid, hei)
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        val size = getRequiredSpace(graphics,fontsize, fontsizeDecrease)
        val halfHeight = (size.y - 1) / 2

        val indentTop = (size.x - divided.getRequiredSpace(graphics,fontsize, fontsizeDecrease).x) / 2
        val indentBottom = (size.x - divisor.getRequiredSpace(graphics, fontsize, fontsizeDecrease).x) / 2

        val anchorTop = Vector2i(lmAnchor.x + indentTop, lmAnchor.y - halfHeight / 2)
        val anchorBottom = Vector2i(lmAnchor.x + indentBottom, lmAnchor.y + halfHeight / 2)

        divided.render(anchorTop, graphics, fontsize, fontsizeDecrease)
        divisor.render(anchorBottom, graphics, fontsize, fontsizeDecrease)

        graphics.drawLine(lmAnchor.x, lmAnchor.y, lmAnchor.x+size.x, lmAnchor.y)
    }
}

class FormulaBracketElement(private val content: FormulaElement): FormulaElement(){
    companion object{
        private const val BRACKET_ASPECT = 2.5
    }

    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        val innerSize = content.getRequiredSpace(graphics, fontsize, fontsizeDecrease)
        return Vector2i(innerSize.x + 6 + (2 * (innerSize.y + 8) / BRACKET_ASPECT).toInt(), innerSize.y + 8)
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        val size = getRequiredSpace(graphics,fontsize, fontsizeDecrease)
        val contentSize = content.getRequiredSpace(graphics, fontsize, fontsizeDecrease)
        graphics.font = graphics.font.deriveFont(size.y.toFloat())
        graphics.drawString("(",lmAnchor.x, lmAnchor.y + size.y / 3)
        graphics.drawString(")",lmAnchor.x + size.x - (size.y / BRACKET_ASPECT).toInt(),lmAnchor.y + size.y / 3)

        val contentCenterX = (size.x - contentSize.x) / 2

        content.render(Vector2i(lmAnchor.x + contentCenterX,lmAnchor.y),graphics, fontsize, fontsizeDecrease)
    }
}

class FormulaPowElement(private val mantisse: FormulaElement, private val exponent: FormulaElement): FormulaElement(){
    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        val mantisseSpace = mantisse.getRequiredSpace(graphics, fontsize, fontsizeDecrease)
        val exponentSpace = exponent.getRequiredSpace(graphics, fontsize - fontsizeDecrease, fontsizeDecrease)

        val yOverhead = max(exponentSpace.y / 2 - mantisseSpace.y / 4, 0)

        return Vector2i(mantisseSpace.x + exponentSpace.x, mantisseSpace.y + yOverhead)
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        val mantisseSpace = mantisse.getRequiredSpace(graphics, fontsize, fontsizeDecrease)

        mantisse.render(lmAnchor,graphics, fontsize, fontsizeDecrease)
        val xOffset = mantisseSpace.x
        val yOffset = mantisseSpace.y / 4
        exponent.render(Vector2i(lmAnchor.x + xOffset, lmAnchor.y - yOffset),graphics, fontsize - fontsizeDecrease, fontsizeDecrease)
    }
}

class FormulaSubElement(private val base: FormulaElement, private val index: FormulaElement): FormulaElement(){
    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        val baseSpace = base.getRequiredSpace(graphics, fontsize, fontsizeDecrease)
        val indexSpace = index.getRequiredSpace(graphics, fontsize - fontsizeDecrease, fontsizeDecrease)

        val yOverhead = max(indexSpace.y / 2 - baseSpace.y / 4, 0)

        return Vector2i(baseSpace.x + indexSpace.x, baseSpace.y + yOverhead)
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        val baseSpace = base.getRequiredSpace(graphics, fontsize, fontsizeDecrease)

        base.render(lmAnchor,graphics, fontsize, fontsizeDecrease)
        val xOffset = baseSpace.x
        val yOffset = baseSpace.y / 4
        index.render(Vector2i(lmAnchor.x + xOffset, lmAnchor.y + yOffset),graphics, fontsize - fontsizeDecrease, fontsizeDecrease)
    }
}

class FormulaJoinElement(private vararg val elements: FormulaElement): FormulaElement(){
    override fun getRequiredSpace(graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int): Vector2i {
        val spaces = elements.map{it.getRequiredSpace(graphics, fontsize, fontsizeDecrease)}
        return Vector2i(spaces.sumOf{it.x} + 3 * (spaces.size - 1), spaces.maxOf{it.y})
    }

    override fun render(lmAnchor: Vector2i, graphics: Graphics2D, fontsize: Int, fontsizeDecrease: Int) {
        var xOffset = 0
        elements.forEach{
            it.render(Vector2i(lmAnchor.x+xOffset, lmAnchor.y), graphics, fontsize, fontsizeDecrease)
            xOffset += it.getRequiredSpace(graphics, fontsize, fontsizeDecrease).x + 3
        }
    }
}

fun String.toFormulaElement(): FormulaTextElement{
    return FormulaTextElement(this)
}

fun FormulaElement.bracket(): FormulaBracketElement{
    return FormulaBracketElement(this)
}

fun FormulaElement.pow(other: FormulaElement): FormulaPowElement{
    return FormulaPowElement(this, other)
}

fun FormulaElement.sub(other: FormulaElement): FormulaSubElement{
    return FormulaSubElement(this, other)
}

class FormulaVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val formula: FormulaElement,
    private val configuration: StaticFormulaVideoClipConfiguration = StaticFormulaVideoClipConfiguration()
): VideoClip(id, size, position, visible) {
    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val curSize = Vector2i(img.width, img.height)

        val graphics = img.createGraphics()

        val lmAnchor = Vector2i(0,curSize.y / 2)
        formula.render(lmAnchor, graphics, configuration.baseFontSize, configuration.fontSizeDecreasePerLevel)
    }
}