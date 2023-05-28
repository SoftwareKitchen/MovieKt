# MovieKt
## What is MovieKt
MovieKt is a Library for Kotlin (and thereby Java) which can create Videos from
Java Images using FFMPEG - similarly to MoviePy.  
Currently only MP4 videos are supported
## How does it work
### Create a Movie
``` kotlin
import tech.softwarekitchen.moviekt.core.Movie
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

## Examples
There are examples in src/main/examples folder

## Clips 
Everything that resembles an object in a video is a Clip, a Video or AudioClip.  
VideoClips are organized in a tree structure, AudioClips can be merged via an AudioContainerClip.

## Existing implementations
A clip provides image information for a rectangle and needs to extend tech.softwarekitchen.moviekt.clips.Clip  
A clip can contain subclips, children will be rendered in the same order as they were added -> the last clip added will be on top

Current implementations:  

### Video
* Basic
  * ContainerVideoClip - Clip container without its own content
  * SingleColorVideoClip - Single color rectangle (filled)
* Image
  * ImageSlideshowVideoClip - Clip with multiple images switching
  * StaticImageVideoClip - Clip with a single image
  * SVGVideoClip - Clip that paints a SVG
* Shape
  * ArrowVideoClip - An arrow
  * Dynamic2DSceneVideoClip - 2D animations
* Text
  * TextVideoClip - Text with transparent background
  * StaticFormulaVideoVlip - Scientific formula
* Data
  * DataTableVideoClip - Simple grid
* Diagrams
  * DynamicHorizontalBarDiagramVideoClip - horizontal bar diagram
  * DynamicHorizontalSpanDiagramVideoClip - horizontal bar diagram without origin at 0
  * DynamicLineDiagramVideoClip - line diagram
  * DynamicPointDiagramVideoClip - point diagram
  * DynamicVerticalBarDiagramVideoClip - vertical bar diagram
  * DynamicTrajectoryDiagramVideoClip - trajectory diagram

### Animations
Animations reflect all changes in the video. Each animation will change a node's property
and therefore trigger a redraw.

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
