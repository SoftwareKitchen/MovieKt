package tech.softwarekitchen.moviekt.mutation.impl

import tech.softwarekitchen.common.vector.Vector2i
import tech.softwarekitchen.moviekt.mutation.MovieKtMutation

class MoveMutation(node: String, start: Float, duration: Float, private val tgt: Vector2i): MovieKtMutation("move",start, duration, node, mapOf("target" to mapOf("x" to tgt.x, "y" to tgt.y))) {
    override fun shift(t: Float): MovieKtMutation {
        return MoveMutation(
            node, start + t, duration, tgt
        )
    }
}
