package tech.softwarekitchen.movitkt.animation

interface MovieKtAnimation<T: Any> {
    val nodeId: String
    val property: String
    fun isApplicable(t: Float): Boolean
    fun isFinished(t: Float): Boolean
    fun get(t: Float): T
    fun shift(t: Float): MovieKtAnimation<T>
}
