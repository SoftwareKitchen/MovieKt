package tech.softwarekitchen.moviekt.clips.code

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.code.parser.YMLParser
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.text.getTextSize
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.ceil
import kotlin.math.roundToInt

data class CodeVideoClipTheme(
    val normColor: Color = Color.WHITE,
    val keywordColor: Color = Color.ORANGE,
    val highlightColor: Color = Color(100,120,0)
)
data class CodeVideoClipConfiguration(val format: String, val file: File, val lineNumbers: Boolean = true, val theme: CodeVideoClipTheme = CodeVideoClipTheme())

enum class CodeSnippetType{
    Normal, Keyword
}
interface CodeSnippet
data class TextCodeSnippet(val text: String, val type: CodeSnippetType): CodeSnippet
data class IndentCodeSnippet(val levels: Int): CodeSnippet
data class CodeLine(val snippets: List<CodeSnippet>)
data class Code(val lines: List<CodeLine>)

interface CodePrettifier{
    fun prettify(code: String): Code
}

class CodeVideoClip(
    id: String, size: Vector2i, position: Vector2i,
    private val configuration: CodeVideoClipConfiguration
): VideoClip(
    id, size, position
) {
    companion object{
        val PropertyKey_LineOffset = "LineOffset"
        val PropertyKey_HighlightStart = "HighlightStart"
        val PropertyKey_HighlightEnd = "HighlightEnd"

        val formatters = mapOf(
            "YML" to YMLParser()
        )
    }

    private val formatted: Code
    private val highlightStart = VideoClipProperty(PropertyKey_HighlightStart, 0.0, this::markDirty)
    private val highlightEnd = VideoClipProperty(PropertyKey_HighlightEnd, 0.0, this::markDirty)
    private val offsetProperty = VideoClipProperty(PropertyKey_LineOffset, 0.0, this::markDirty)
    init{
        val code = configuration.file.readText()
        formatted = formatters[configuration.format]!!.prettify(code)

        registerProperty(offsetProperty, highlightStart, highlightEnd)
    }

    override fun renderContent(img: BufferedImage) {
        val fontSize = 18
        val lineHeight = 22
        val indentWidth = 15

        val g = img.createGraphics()
        g.font = g.font.deriveFont(fontSize.toFloat())

        if(highlightEnd.v > highlightStart.v){
            val y0 = ((highlightStart.v - offsetProperty.v) * lineHeight).roundToInt()
            val y1 = ((highlightEnd.v - offsetProperty.v) * lineHeight).roundToInt()
            g.color = configuration.theme.highlightColor
            g.fillRect(0,y0,img.width,y1-y0)
        }

        val lineNumberPadding = 3
        val separatorWidth = 2
        val lineNumberSpace = 2 * lineNumberPadding + ceil(formatted.lines.indices.map{"$it".getTextSize(g.font).width}.max()).toInt()
        val lineNumberAlignRight = lineNumberSpace - lineNumberPadding
        val textBase = when(configuration.lineNumbers){
            false -> 0
            else -> lineNumberSpace + lineNumberPadding + separatorWidth
        }

        if(configuration.lineNumbers){
            g.color = Color.LIGHT_GRAY
            g.fillRect(lineNumberSpace,0,separatorWidth, img.height)
        }

        formatted.lines.forEachIndexed{
            i, cl ->
            val y = ((3 * (i - offsetProperty.v) + 2) * lineHeight / 3).roundToInt()
            if(configuration.lineNumbers){
                val req = ceil("$i".getTextSize(g.font).width).toInt()
                val lnx = lineNumberAlignRight - req
                g.color = configuration.theme.normColor
                g.drawString("$i",lnx, y)
            }

            var x = textBase
            cl.snippets.forEach{
                sn ->
                when{
                    sn is IndentCodeSnippet -> {
                        x += indentWidth * sn.levels
                    }
                    sn is TextCodeSnippet -> {
                        when(sn.type){
                            CodeSnippetType.Normal -> g.color = configuration.theme.normColor
                            CodeSnippetType.Keyword -> g.color = configuration.theme.keywordColor
                        }
                        g.drawString(sn.text, x, y)
                        x += ceil(sn.text.getTextSize(g.font).width).toInt()
                    }
                }
            }
        }
    }
}