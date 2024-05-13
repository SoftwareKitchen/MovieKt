package tech.softwarekitchen.moviekt.core.exception

class UnknownPropertyException(property: String, id: String): Exception("Unknown property $property in node $id")
class VideoIsClosedException: Exception()
class ImageSizeMismatchException: Exception()
class FFMPEGDidntShutdownException: Exception()
class NodeNotFoundException(name: String): Exception("Node not found: '$name'")
