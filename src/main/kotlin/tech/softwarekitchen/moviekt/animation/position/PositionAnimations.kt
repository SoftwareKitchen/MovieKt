package tech.softwarekitchen.moviekt.animation.position

import tech.softwarekitchen.common.vector.Vector2i

typealias PositionProvider = (Int, Int, Float) -> Vector2i

fun Vector2i.toStaticPositionProvider(): PositionProvider{
    return {_,_,_ -> this}
}

fun PositionProvider.moveLinear(to: Vector2i, start: Float, duration: Float): PositionProvider{
    return {
        cur,tot,t ->
        if(t < start){
            this(cur, tot, t)
        }else if (t > start + duration){
            to
        }else{
            val q = (t - start) / duration
            val base = this(cur, tot, t)
            Vector2i((base.x * (1-q) + to.x * q).toInt(), (base.y * (1-q) + to.y * q).toInt())
        }
    }
}
