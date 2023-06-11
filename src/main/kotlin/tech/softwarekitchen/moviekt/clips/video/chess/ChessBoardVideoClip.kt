package tech.softwarekitchen.moviekt.clips.video.chess

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.clips.video.VideoClip
import tech.softwarekitchen.moviekt.clips.video.VideoClip.Companion.PropertyKey_Position
import tech.softwarekitchen.moviekt.clips.video.VideoClip.Companion.PropertyKey_Size
import tech.softwarekitchen.moviekt.clips.video.image.svg.SVGVideoClip
import tech.softwarekitchen.moviekt.clips.video.image.svg.SVGVideoClipConfiguration
import tech.softwarekitchen.moviekt.mutation.MovieKtMutation
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

private enum class ChessPieceType(val v: String){
    King("K"), Queen("Q"), Bishop("B"), Knight("N"), Rook("R"), Pawn("P")
}

private enum class ChessPieceColor(val c: String){
    Black("b"), White("w")
}

private fun Char.parseRow(): Int{
    return digitToInt() - 1
}
private fun Char.parseCol(): Int{
    return code - 'a'.code
}

private class ChessPiece(desc: String, private var imgSize: Vector2i) {
    val type: ChessPieceType
    val color: ChessPieceColor
    var col: Int
    var row: Int
    private var currentMutation: String? = null
    private var sourceRow: Int? = null
    private var sourceCol: Int? = null

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

        row = desc[desc.length-1].parseRow()
        col = desc[desc.length-2].parseCol()

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

    fun updateBoardSize(size: Vector2i){
        imgSize = size
        val updatedSize = imgSize.scale(0.125)
        val zeroPrevent = Vector2i(max(1, updatedSize.x), max(1, updatedSize.y))
        piece.set(PropertyKey_Size, zeroPrevent)
        val pos = Vector2i(
            imgSize.x * col / 8,
            imgSize.y * (7 - row) / 8
        )
        piece.set(PropertyKey_Position, pos)
    }

    fun moveTo(col: Int, row: Int): String{
        val uuid = UUID.randomUUID().toString()
        currentMutation = uuid
        sourceRow = this.row
        sourceCol = this.col
        this.row = row
        this.col = col

        return uuid
    }

    fun setKeyframe(keyframe: String, v: Float){
        currentMutation?.let{
            if(it == keyframe){
                val src = Vector2i(
                    imgSize.x * sourceCol!! / 8,
                    imgSize.y * (7 - sourceRow!!) / 8
                )
                val tgt = Vector2i(
                    imgSize.x * col / 8,
                    imgSize.y * (7 - row) / 8
                )
                val pos = Vector2i(
                    (src.x * (1f - v) + tgt.x * v).roundToInt(),
                    (src.y * (1f - v) + tgt.y * v).roundToInt()
                )
                piece.set(VideoClip.PropertyKey_Position, pos)
            }
        }
    }

    fun removeMutation(kf: String){
        if(currentMutation == kf){
            currentMutation = null
            sourceRow = null
            sourceCol = null
        }
        val pos = Vector2i(
            imgSize.x * col / 8,
            imgSize.y * (7 - row) / 8
        )
        piece.set(VideoClip.PropertyKey_Position, pos)

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

    companion object{
        val PropertyKey_BoardState = "State"
    }

    private val pieces = ArrayList<ChessPiece>()
    private val boardStateProperty = VideoClipProperty(PropertyKey_BoardState,configuration.position,this::updateBoardState)
    init{
        registerMutation("chess_move",this::prepareMoveMutation)
        registerProperty(boardStateProperty)

        updateBoardState()
    }

    private fun updateBoardState(){
        pieces.forEach{
            removeChild(it.piece)
        }

        pieces.clear()

        val boardDesc = boardStateProperty.v

        val strParts = boardDesc.split(" ").map{it.trim()}.filter{ it.isNotBlank() }
        val updatedPieces = strParts.map{
            ChessPiece(it, getSize())
        }.toMutableList()

        updatedPieces.forEach{
            addChild(it.piece)
        }
        this.pieces.addAll(updatedPieces)

        markDirty()
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

    private fun prepareMoveMutation(mutation: MovieKtMutation): Pair<String, ActiveMutation>{
        val chessMove = mutation.base["move"] as String

        val figureType = when(chessMove[0]){
            'K' -> ChessPieceType.King
            'Q' -> ChessPieceType.Queen
            'B' -> ChessPieceType.Bishop
            'N' -> ChessPieceType.Knight
            'R' -> ChessPieceType.Rook
            else -> ChessPieceType.Pawn
        }

        val rest = when(figureType){
            ChessPieceType.Pawn -> chessMove
            else -> chessMove.substring(1, chessMove.length)
        }

        //Identify target
        val targetRow = rest[4].parseRow()
        val targetCol = rest[3].parseCol()

        //Identify source
        val sourceCol = rest[0].parseCol()
        val sourceRow = rest[1].parseRow()
        val piece = pieces.first{it.type == figureType && it.col == sourceCol && it.row == sourceRow}

        val id = piece.moveTo(targetCol, targetRow)
        val tickCB: (Float) -> Unit = { piece.setKeyframe(id, it) }
        val closeCB: () -> Unit = {piece.removeMutation(id)}

        return Pair(id, ActiveMutation(tickCB, closeCB))
    }

    override fun onResize() {
        pieces.forEach{
            it.updateBoardSize(getSize())
        }
    }
}
