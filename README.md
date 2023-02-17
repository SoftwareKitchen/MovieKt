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
    val rootClip = SingleColorClip(Vector2i(0,0),Vector2i(640,480),Color(128,64,64))
    val movie = Movie("foo.mp4",5,25,rootClip)
    movie.write(rootClip)
}

```

## Clips 
Everything that resembles an object in a video is a Clip, a Video or AudioClip.  
VideoClips are organized in a tree structure, AudioClips can be merged via an AudioContainerClip.

## Existing implementations
A clip provides image information for a rectangle and needs to extend tech.softwarekitchen.moviekt.clips.Clip  
A clip can contain subclips, children will be rendered in the same order as they were added -> the last clip added will be on top

Current implementations:  
  
Definitions:  
static := a fixed value will be used for the entire duration  
dynamic := the value will be updated frame by frame, therefore it has to be provided via a callback

### Video
* Basic
  * ContainerVideoClip - Clip container without its own content
  * SingleColorVideoClip - Single color rectangle (filled)
* Text
  * StaticTextVideoClip - Text with transparent background  
  * DynamicTextVideoClip - Text with transparent background
* Diagrams
  * DynamicLineDiagramVideoClip - line diagram
  * DynamicPointDiagramVideoClip - point diagram
  * DynamicVerticalBarDiagramVideoClip - vertical bar diagram

### Audio
* AudioContainerClip - combination of other AudioClips, currently used for organizing sound in a video
* FileAudioClip - Clip generated from a soundfile

## Create your own implementation
### Video
To create your own clip implementation extend the abstract class VideoClip
``` kotlin
abstract fun renderContent(frameNo: Int, nFrames: Int, tTotal: Float, tInternal: Float): BufferedImage
```
### Audio
To create your own clip implementation extend the abstract class AudioClip
``` kotlin
abstract fun getLength(): Double
abstract fun getAt(t: Double): Double
```
