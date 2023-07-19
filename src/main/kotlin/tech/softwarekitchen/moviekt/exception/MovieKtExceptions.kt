package tech.softwarekitchen.moviekt.exception

class VideoIsClosedException: Exception()
class ImageSizeMismatchException: Exception()
class FFMPEGDidntShutdownException: Exception()
class ClipSizeMismatchException: Exception()
class ChainedClipRequiresVisibilityDurationException: Exception()
class UnknownPropertyException(property: String, id: String): Exception("Unknown property $property in node $id")
class InvalidConfigurationException(msg: String): Exception(msg)
class NodeNotFoundException(name: String): Exception("Node not found: '$name'")
