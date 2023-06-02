package tech.softwarekitchen.moviekt.clips.video.image.svg.model

class SVGGroup(data: Map<String, Any>) {
    val subgroups: List<SVGGroup>
    val paths: List<SVGPath>
    val styles: List<SVGStyle>
    val circles: List<SVGCircle>

    init{
        subgroups = if(data.containsKey("g")){
            val group_s = data["g"]!!
            when{
                group_s is Map<*,*> -> {
                    //One subgroup
                    listOf(SVGGroup(group_s as Map<String, Any>))
                }
                group_s is List<*> -> {
                    //Multiple subgroups
                    (group_s as List<Map<String, Any>>).map(::SVGGroup)
                }
                else -> throw Exception()
            }
        }else{
            listOf()
        }

        paths = if(data.containsKey("path")){
            val path = data["path"]
            when{
                path is Map<*,*> -> {
                    listOf(SVGPath(path as Map<String, Any>))
                }
                path is List<*> -> {
                    (path as List<Map<String, Any>>).map(::SVGPath)
                }
                else -> throw Exception()
            }
        }else{
            listOf()
        }

        circles = if(data.containsKey("circle")){
            val circles = data["circle"]
            when{
                circles is Map<*,*> -> {
                    listOf(SVGCircle(circles as Map<String, Any>))
                }
                circles is List<*> -> {
                    (circles as List<Map<String, Any>>).map(::SVGCircle)
                }
                else -> throw Exception()
            }
        }else{
            listOf()
        }

        styles = parseSVGStyles(data["style"]?.let{it as String})
    }
}