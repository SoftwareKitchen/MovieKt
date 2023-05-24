package tech.softwarekitchen.moviekt.clips.code.parser

import tech.softwarekitchen.moviekt.clips.code.*

class YMLParser: CodePrettifier {
    override fun prettify(code: String): Code {
        val lines = code.split("\n").filter{it.trim().isNotBlank()}
        val indents = arrayListOf(0)

        val list = ArrayList<CodeLine>()
        lines.forEach{
            val lineElements = ArrayList<CodeSnippet>()
            val whitespaces = it.indexOf(it.trim())
            val subIndex = when(indents.contains(whitespaces)){
                true -> indents.indexOf(whitespaces)
                false -> {
                    if(whitespaces < indents.last()){
                        throw Exception()
                    }
                    indents.add(whitespaces)
                    indents.size - 1
                }
            }
            if(subIndex > 0){
                lineElements.add(IndentCodeSnippet(subIndex))
            }
            val rest = it.trim()
            val rest2 = if(rest.startsWith("-")){
                lineElements.add(TextCodeSnippet("-",CodeSnippetType.Normal))
                rest.substring(1, rest.length).trim()
            }else{
                rest
            }

            if(rest2.contains(":")){
                val parts = rest2.split(":")
                lineElements.add(TextCodeSnippet(parts[0],CodeSnippetType.Keyword))
                lineElements.add(TextCodeSnippet(": ${parts[1]}", CodeSnippetType.Normal))
            }else{
                lineElements.add(TextCodeSnippet(rest2, CodeSnippetType.Normal))
            }

            list.add(CodeLine(lineElements))
        }

        return Code(list)
    }
}
