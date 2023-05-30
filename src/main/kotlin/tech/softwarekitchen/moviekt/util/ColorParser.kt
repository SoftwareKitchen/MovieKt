package tech.softwarekitchen.moviekt.util

import java.awt.Color

private val regex = "^#[\\dabcdef]{6}$".toRegex()
fun parseColor(raw: String): Color {
    if(!regex.matches(raw)){
        throw Exception()
    }

    val r = raw.substring(1,3).toInt(16)
    val g = raw.substring(3,5).toInt(16)
    val b = raw.substring(5,7).toInt(16)

    return Color(r,g,b)
}
