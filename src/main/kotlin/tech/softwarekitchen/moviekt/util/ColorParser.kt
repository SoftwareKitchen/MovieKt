package tech.softwarekitchen.moviekt.util

import java.awt.Color

private val regex = "^#[\\dabcdef]{6}$".toRegex()
private val regexShort = "^#[\\dabcdef]{3}$".toRegex()
private val regexRgb = "^rgb\\s*\\(\\s*\\d+\\s*,\\s*\\d+\\s*,\\s*\\d+\\s*\\)$".toRegex()
fun parseColor(raw: String): Color {
    if(regexShort.matches(raw)){
        val r = raw.substring(1,2).toInt(16) * 17
        val g = raw.substring(2,3).toInt(16) * 17
        val b = raw.substring(3,4).toInt(16) * 17

        return Color(r,g,b)
    }

    if(regex.matches(raw)){
        val r = raw.substring(1,3).toInt(16)
        val g = raw.substring(3,5).toInt(16)
        val b = raw.substring(5,7).toInt(16)

        return Color(r,g,b)
    }

    if(regexRgb.matches(raw)){
        val inner = raw.substring(raw.indexOf("(")+1, raw.indexOf(")"))
        val parts = inner.split(",").map(String::trim)
        val r = parts[0].toInt()
        val g = parts[1].toInt()
        val b = parts[2].toInt()

        return Color(r,g,b)
    }

    throw Exception()

}
