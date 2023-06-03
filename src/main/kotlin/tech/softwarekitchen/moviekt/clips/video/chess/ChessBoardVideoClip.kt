package tech.softwarekitchen.moviekt.clips.video.chess

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.image.svg.SVGVideoClip
import tech.softwarekitchen.moviekt.clips.video.image.svg.SVGVideoClipConfiguration
import tech.softwarekitchen.moviekt.core.Movie
import java.awt.BasicStroke
import java.awt.Color
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.File
import javax.imageio.ImageIO

private enum class ChessPieceType(val v: String){
    King("K"), Queen("Q"), Bishop("B"), Knight("N"), Rook("R"), Pawn("P")
}

private enum class ChessPieceColor(val c: String){
    Black("b"), White("w")
}

private class ChessPiece(desc: String, imgSize: Vector2i) {
    val type: ChessPieceType
    val color: ChessPieceColor
    val col: Int
    val row: Int

    val piece: SVGVideoClip

    init{
        color = when(desc[0]){
            'b' -> ChessPieceColor.Black
            'w' -> ChessPieceColor.White
            else -> throw Exception()
        }
        val t = when(desc.length){
            3 -> 'P'
            else -> desc[1]
        }
        type = when(t){
            'K' -> ChessPieceType.King
            'Q' -> ChessPieceType.Queen
            'B' -> ChessPieceType.Bishop
            'N' -> ChessPieceType.Knight
            'R' -> ChessPieceType.Rook
            'P' -> ChessPieceType.Pawn
            else -> throw Exception()
        }

        row = desc[desc.length-1].digitToInt() - 1
        col = desc[desc.length-2].code - 'a'.code

        val xPos = imgSize.x * col / 8
        val yPos = imgSize.y * (7 - row) / 8
        val svg = File(javaClass.getResource("/chess/${color.c}${type.v}.svg")!!.toURI())
        piece = SVGVideoClip(
            "_",
            imgSize.scale(0.125),
            Vector2i(xPos, yPos),
            true,
            SVGVideoClipConfiguration(svg)
        )
    }

}

data class ChessBoardVideoClipConfiguration(
    val position: String = "bKe8 wKe1 bQd8 wQd1 bBc8 bBf8 wBc1 wBf1 bNb8 bNg8 wNb1 wNg1 bRa8 bRh8 wRa1 wRh1 ba7 bb7 bc7 bd7 be7 bf7 bg7 bh7 wa2 wb2 wc2 wd2 we2 wf2 wg2 wh2"
)
class ChessBoardVideoClip(
    id: String,
    size: Vector2i,
    position: Vector2i,
    visible: Boolean,
    private val configuration: ChessBoardVideoClipConfiguration
): VideoClip(
    id, size, position, visible
) {

    private val pieces: MutableList<ChessPiece>
    init{
        val strParts = configuration.position.split(" ").map{it.trim()}.filter{ it.isNotBlank() }
        pieces = strParts.map{
            println(it)
            ChessPiece(it, size)
        }.toMutableList()

        pieces.forEach{
            addChild(it.piece)
        }
    }

    override fun renderContent(img: BufferedImage) {
        val xBounds = (0..8).map{img.width * it / 8}
        val yBounds = (0..8).map{img.height * it / 8}

        val g = img.createGraphics()
        g.color = Color.LIGHT_GRAY
        g.fillRect(0,0,img.width, img.height)

        g.color = Color.DARK_GRAY
        for(x in 0 until 8){
            for(y in 0 until 8){
                if((x+y) % 2 == 0){
                    g.fillRect(xBounds[x],yBounds[7 - y],xBounds[x+1] - xBounds[x], yBounds[8 - y] - yBounds[7 - y])
                }
            }
        }
    }
}
