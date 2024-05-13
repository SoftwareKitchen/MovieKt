package tech.softwarekitchen.moviekt.clips.video.code.parser

import tech.softwarekitchen.moviekt.clips.video.code.*

private abstract class KotlinParserStage(
    val type: CodeSnippetType,
    val next: KotlinParserStage? = null
){
    abstract fun parseStep(text: String): List<IntRange>
    fun parse(text: String): List<CodeSnippet>{
        val result = ArrayList<CodeSnippet>()
        val found = parseStep(text)
        found.forEachIndexed{
            i, r ->
            val lastEnd = when{
                i == 0 -> 0
                else -> found[i-1].last+1
            }
            val raw = text.substring(lastEnd, r.first)
            if(raw.isNotEmpty()){
                when(next){
                    null -> result.add(TextCodeSnippet(raw, CodeSnippetType.Normal ))
                    else -> result.addAll(next.parse(raw))
                }
            }
            val found = text.substring(r.first, r.last+1)
            result.add(TextCodeSnippet(found, type))
        }
        val lastStart = when(found.isEmpty()){
            true -> 0
            else -> found.last().last+1
        }
        val last = text.substring(lastStart, text.length)
        if(last.isNotEmpty()){
            when(next){
                null -> result.add(TextCodeSnippet(last, CodeSnippetType.Normal))
                else -> result.addAll(next.parse(last))
            }
        }
        return result
    }
}

private class KotlinCommentConstantParserStage: KotlinParserStage(CodeSnippetType.Comment, next = KotlinStringConstantParserStage()){
    override fun parseStep(text: String): List<IntRange> {
        if(!text.contains("//")){
            return listOf()
        }
        val index = text.indexOf("//")
        return listOf(IntRange(index, text.length - 1))
    }
}
private class KotlinStringConstantParserStage: KotlinParserStage(CodeSnippetType.TextConstant, next = KotlinKeywordParserStage()){
    override fun parseStep(text: String): List<IntRange> {
        val doubleQuotes = text.indices.filter{text[it] == '"'}
        return (0 until doubleQuotes.size / 2).map{
            IntRange(doubleQuotes[it*2], doubleQuotes[it*2+1])
        }
    }
}

private abstract class KotlinRegexStage(private val regexes: List<Regex>, type: CodeSnippetType, next: KotlinParserStage? = null): KotlinParserStage(type, next = next){
    override fun parseStep(text: String): List<IntRange> {
        val result = ArrayList<IntRange>()
        regexes.forEach{
            val matches = it.findAll(text)
            val partRes = matches.map{it.range}
            result.addAll(partRes)
        }

        return result.sortedBy { it.first }
    }
}
private class KotlinKeywordParserStage: KotlinRegexStage(
    listOf(
        "\\bval\\b".toRegex(),
        "\\bfun\\b".toRegex()
    ), CodeSnippetType.Keyword,
    KotlinNumericConstantParserStage()
)

private class KotlinNumericConstantParserStage: KotlinRegexStage(
    listOf("\\b[\\d.]+\\b".toRegex()), CodeSnippetType.NumericConstant
)

class KotlinParser: CodePrettifier {
    private val parser = KotlinCommentConstantParserStage()
    override fun prettify(code: String): Code {
        return Code(code.split("\n").map{
            CodeLine(parser.parse(it))
        })
    }
}
