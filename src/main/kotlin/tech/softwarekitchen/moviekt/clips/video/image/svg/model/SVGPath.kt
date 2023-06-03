package tech.softwarekitchen.moviekt.clips.video.image.svg.model

import org.w3c.dom.Element

class SVGPath(data: Element): SVGItem{
    val operations: List<SVGOperation>
    val styles: List<SVGStyle>

    init{

        val path = data.attributes.getNamedItem("d")!!.textContent
        styles = data.parseSVGStyles()

        val ops = ArrayList<SVGOperation>()
        var rest = path

        val splitOffNumber: () -> Double = {
            if(rest[0] == ','){
                rest = rest.substring(1, rest.length)
            }
            val negate  = when(rest[0] == '-'){
                true -> {
                    rest = rest.substring(1,rest.length)
                    -1
                }
                else -> +1
            }
            val firstIndex = rest.indexOfFirst { !it.isDigit() && it != '.' }
            if(firstIndex < 0){
                val v = negate * rest.toDouble()
                rest = ""
                v
            }else{
                val part = rest.substring(0,firstIndex)
                rest = rest.substring(firstIndex, rest.length).trim()
                negate * part.toDouble()
            }
        }
        val readCooPair: () -> Pair<Double, Double> = {
            Pair(splitOffNumber(), splitOffNumber())
        }
        val readFlag: () -> Int = {
            val c = rest.substring(0,1)
            rest = rest.substring(1,rest.length).trim()
            c.toInt()
        }

        while(rest != ""){
            if(rest[0].isLetter()){
                val letter = rest[0]
                rest = rest.substring(1, rest.length).trim()

                val op = when(letter){
                    'C' -> SVGCubicBezierOperation(readCooPair(), readCooPair(), readCooPair())
                    'c' -> SVGRelativeCubicBezierOperation(readCooPair(), readCooPair(), readCooPair())
                    'M' -> SVGMoveOperation(readCooPair())
                    'm' -> SVGRelativeMoveOperation(readCooPair())
                    'A' -> {
                        SVGArcOperation(readCooPair(),splitOffNumber(), readFlag(), readFlag(),readCooPair())
                    }
                    'a' -> {
                        SVGRelativeArcOperation(readCooPair(),splitOffNumber(), readFlag(), readFlag(),readCooPair())
                    }
                    'L' -> {
                        SVGLineOperation(readCooPair())
                    }
                    'l' -> {
                        SVGRelativeLineOperation(readCooPair())
                    }
                    'H' -> {
                        SVGHorizontalLine(splitOffNumber())
                    }
                    'h' -> {
                        SVGRelativeHorizontalLine(splitOffNumber())
                    }
                    'V' -> {
                        SVGVerticalLine(splitOffNumber())
                    }
                    'v' -> {
                        SVGRelativeVerticalLine(splitOffNumber())
                    }
                    's' -> {
                        SVGRelativeSmoothCubicBezierOperation(readCooPair(), readCooPair())
                    }
                    'Z', 'z' -> {
                        SVGClosePath()
                    }
                    else -> throw Exception()
                }
                ops.add(op)
            }else{
                val lastType = ops.last().type
                when{
                    lastType == SVGOperationType.RelativeLine -> ops.add(SVGRelativeLineOperation(readCooPair()))
                    lastType == SVGOperationType.RelativeCubicBezier -> ops.add(SVGRelativeCubicBezierOperation(readCooPair(), readCooPair(), readCooPair()))
                    else -> ops.add(SVGLineOperation(readCooPair()))
                }
            }
        }

        operations = ops
    }
}
