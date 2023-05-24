package tech.softwarekitchen.moviekt.clips.code

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.code.parser.YMLParser
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.text.getTextSize
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.ceil

data class CodeVideoClipConfiguration(val format: String, val file: File)

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
    configuration: CodeVideoClipConfiguration
): VideoClip(
    id, size, position
) {
    companion object{
        val formatters = mapOf(
            "YML" to YMLParser()
        )
    }

    private val formatted: Code
    init{
        val code = configuration.file.readText()
        formatted = formatters[configuration.format]!!.prettify(code)
    }

    override fun renderContent(img: BufferedImage) {
        val fontSize = 18
        val lineHeight = 22
        val indentWidth = 15
        val padding = 5

        val g = img.createGraphics()
        g.font = g.font.deriveFont(fontSize.toFloat())

        formatted.lines.forEachIndexed{
            i, cl ->
            val y = (3 * i + 2) * lineHeight / 3
            var x = 0
            cl.snippets.forEach{
                sn ->
                when{
                    sn is IndentCodeSnippet -> {
                        x += indentWidth * sn.levels
                    }
                    sn is TextCodeSnippet -> {
                        g.drawString(sn.text, x, y)
                        x += padding + ceil(sn.text.getTextSize(g.font).width).toInt()
                    }
                }
            }
        }
    }
}