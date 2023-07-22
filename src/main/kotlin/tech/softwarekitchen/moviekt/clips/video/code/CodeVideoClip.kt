package tech.softwarekitchen.moviekt.clips.video.code

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoTimestamp
import tech.softwarekitchen.moviekt.clips.video.code.parser.KotlinParser
import tech.softwarekitchen.moviekt.clips.video.code.parser.YMLParser
import tech.softwarekitchen.moviekt.clips.video.text.getTextSize
import tech.softwarekitchen.moviekt.util.DoubleRange
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.ceil
import kotlin.math.roundToInt

data class CodeVideoClipTheme(
    val normColor: Color = Color.WHITE,
    val keywordColor: Color = Color.ORANGE,
    val highlightColor: Color = Color(96,96,96),
    val textConstantColor: Color = Color(0,120,0),
    val commentColor: Color = Color(128,128,128),
    val numericColor: Color = Color(0,200,200)
)
enum class CodeVideoClipAnchor{
    Top, Center
}
data class CodeVideoClipConfiguration(
    val format: String,
    val file: String,
    val lineNumbers: Boolean = true,
    val theme: CodeVideoClipTheme = CodeVideoClipTheme(),
    val ttFont: String? = null,
    val anchor: CodeVideoClipAnchor = CodeVideoClipAnchor.Top,
    val highlight: DoubleRange? = null,
    val lineOffset: Double = 0.0
)
enum class CodeSnippetType{
    Normal, Keyword, TextConstant, Comment, NumericConstant
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
    id: String, size: Vector2i, position: Vector2i,visible: Boolean,
    private val configuration: CodeVideoClipConfiguration
): VideoClip(
    id, size, position, visible
) {
    companion object{
        val PropertyKey_Anchor = "Anchor"
        val PropertyKey_Font = "Font"
        val PropertyKey_LineOffset = "LineOffset"
        val PropertyKey_Highlight = "Highlight"
        val PropertyKey_Source = "Source"

        val formatters = mapOf(
            "YML" to YMLParser(),
            "Kotlin" to KotlinParser()
        )
    }

    private var formatted: Code
    private val anchorProperty =
        VideoClipProperty(
            PropertyKey_Anchor,
            configuration.anchor,
            this::markDirty,
            converter = { CodeVideoClipAnchor.valueOf(it as String)}
        )
    private val fontProperty =
        VideoClipProperty(
            PropertyKey_Font,
            configuration.ttFont,
            {
                font = loadFont()
                markDirty()
            }
        )
    private val sourceProperty =
        VideoClipProperty(
            PropertyKey_Source,
            configuration.file,
            {
                formatted = loadCode()
                markDirty()
            }
        )
    private val highlight =
        VideoClipProperty(
            PropertyKey_Highlight,
            configuration.highlight,
            this::markDirty,
            converter = {
                val conv = it as Map<String, Any>
                val from = conv["from"] as Double
                val to = conv["to"] as Double
                DoubleRange(from, to)
            }
        )
    private val offsetProperty = VideoClipProperty(PropertyKey_LineOffset, configuration.lineOffset, this::markDirty)
    private var font: Font?

    init{
        font = loadFont()

        formatted = loadCode()

        registerProperty(offsetProperty, highlight, anchorProperty, sourceProperty, fontProperty)
    }

    private fun loadCode(): Code {
        val code = File(sourceProperty.v).readText()
        return formatters[configuration.format]!!.prettify(code)
    }

    private fun loadFont(): Font?{
        return fontProperty.v?.let{
            Font.createFont(Font.TRUETYPE_FONT, File(it))
        }
    }

    override fun renderContent(img: BufferedImage, t: VideoTimestamp) {
        val fontSize = 18
        val lineHeight = 22
        val indentWidth = 15

        val yOffset = when(anchorProperty.v){
            CodeVideoClipAnchor.Top -> 0
            CodeVideoClipAnchor.Center -> img.height / 2 - lineHeight / 2
        }

        val g = img.createGraphics()
        g.font = (font ?: g.font).deriveFont(fontSize.toFloat())

        val hl = highlight.v
        if(hl != null){
            //-1 accounts for the one-based line numbers
            val y0 = ((hl.from - 1 - offsetProperty.v) * lineHeight).roundToInt() + yOffset
            val y1 = ((hl.to - 1 - offsetProperty.v) * lineHeight).roundToInt() + yOffset
            g.color = configuration.theme.highlightColor
            g.fillRect(0,y0,img.width,y1-y0)
        }

        val lineNumberPadding = 3
        val separatorWidth = 2
        val lineNumberSpace = 2 * lineNumberPadding + ceil(formatted.lines.indices.map{"${it+1}".getTextSize(g.font).width}.max()).toInt()
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
            val y = ((3 * (i - offsetProperty.v) + 2) * lineHeight / 3).roundToInt() + yOffset
            if(configuration.lineNumbers){
                val req = ceil("${i+1}".getTextSize(g.font).width).toInt()
                val lnx = lineNumberAlignRight - req
                g.color = configuration.theme.normColor
                g.drawString("${i+1}",lnx, y)
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
                            CodeSnippetType.TextConstant -> g.color = configuration.theme.textConstantColor
                            CodeSnippetType.Comment -> g.color = configuration.theme.commentColor
                            CodeSnippetType.NumericConstant -> g.color = configuration.theme.numericColor
                        }
                        g.drawString(sn.text, x, y)
                        x += ceil(sn.text.getTextSize(g.font).width).toInt()
                    }
                }
            }
        }
    }
}
