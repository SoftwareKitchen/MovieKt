# MovieKt
## What is MovieKt
MovieKt is a Library for Kotlin (and thereby Java) which can create Videos from
Java Images using FFMPEG.  
Currently only MP4 videos are supported
## How does it work
### Create a Movie
``` kotlin
import tech.softwarekitchen.moviekt.Movie
import tech.softwarekitchen.moviekt.Vector2i
import tech.softwarekitchen.moviekt.clips.SingleColorClip
import tech.softwarekitchen.moviekt.clips.TextClip
import java.awt.Color

fun main(args: Array<String>) {
    val movie = Movie("foo.mp4",5,25,Vector2i(640,480))
    val rootClip = SingleColorClip(Vector2i(0,0),Vector2i(640,480),Color(128,64,64))
    movie.render(rootClip)
}

```

``` kotlin
/**
 * Movie wrapper class
 * @param name target file name
 * @param length video length in seconds
 * @param fps frames per second
 * @param size width and height of the video
 */
class Movie
```
## Create a Clip
A clip provides image information for a rectangle and needs to extend tech.softwarekitchen.moviekt.clips.Clip  
A clip can contain subclips, children will be rendered in the same order as they were added -> the last clip added will be on top

Current implementations:
* SingleColorClip - Single color rectangle (filled)
* TextClip - Text with transparent background  
