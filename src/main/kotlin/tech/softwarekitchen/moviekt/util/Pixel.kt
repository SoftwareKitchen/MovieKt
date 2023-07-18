package tech.softwarekitchen.moviekt.util

data class Pixel(val a: UByte, val r: UByte, val g: UByte, val b: UByte){
    override fun toString(): String {
        return "a$a r$r g$g b$b"
    }
}
