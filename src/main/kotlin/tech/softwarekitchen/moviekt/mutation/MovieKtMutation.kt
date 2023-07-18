package tech.softwarekitchen.moviekt.mutation

open class MovieKtMutation (
    val type: String,
    val start: Float,
    val duration: Float,
    val node: String,
    val base: Map<String, Any>
)
