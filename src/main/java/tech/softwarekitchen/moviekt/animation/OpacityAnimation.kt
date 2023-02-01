package tech.softwarekitchen.moviekt.animation

class OpacityAnimation(partialAnimation: (Float, Float?, Float?) -> Float?): ChainableAnimation<Float>(partialAnimation, 1f)

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

