package tech.softwarekitchen.moviekt.core.mutation

abstract class MovieKtMutation (
    val type: String,
    val start: Float,
    val duration: Float,
    val node: String,
    val base: Map<String, Any>
){
    abstract fun shift(t: Float): MovieKtMutation
}
