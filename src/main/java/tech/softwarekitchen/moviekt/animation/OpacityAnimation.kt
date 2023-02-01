package tech.softwarekitchen.moviekt.animation

class OpacityAnimation(private val partialAnimation: (Float, Float?, Float?) -> Float?){
    private var next: OpacityAnimation? = null
    fun and(next: OpacityAnimation):  OpacityAnimation{
        when(val n = this.next){
            null -> this.next = next
            else -> n.and(next)
        }
        return this
    }

    fun build(): (Float, Float?, Float?) -> Float{
        val nextInChain = when(val n = next){
            null -> null
            else -> n.build()
        }
        return {
            tAbs, tTot, tRel ->
            when(val internalResult = partialAnimation(tAbs, tTot, tRel)){
                null -> {
                    when(val n = nextInChain){
                        null -> 1f
                        else -> n(tAbs, tTot, tRel)
                    }
                }
                else -> internalResult
            }
        }
    }
}

class OpacityAnimationFactory{
    companion object{
        fun createLinearAppear(duration: Float): OpacityAnimation{
            return OpacityAnimation{
                    tAbs,_ , _ ->
                if(tAbs > duration){
                    null
                }else{
                    tAbs / duration
                }
            }
        }
        fun createLinearDisappear(duration: Float, start: Float? = null): OpacityAnimation{
            return OpacityAnimation{
                tAbs, tTot, _ ->
                val start = when(start){
                    null -> tTot!! - duration
                    else -> start
                }
                val delta = tAbs - start
                if(delta < 0){
                    null
                }else if(delta > duration){
                    0f
                }else{
                    1f - delta / duration
                }
            }
        }
    }
}

