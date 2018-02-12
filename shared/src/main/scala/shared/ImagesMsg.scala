package shared

import julienrf.json.derived
import play.api.libs.json.OFormat

sealed trait ImagesMsg

object ImagesMsg {
  implicit val jsonFormat: OFormat[ImagesMsg] = derived.oformat[ImagesMsg]()
}

case class ImageDataListMsg(imageDataList: ImageDataList) extends ImagesMsg

case class ImageDataMsg(imageDataList: ImageData) extends ImagesMsg

// as with akka-http the web-socket connection will be closed when idle for too long.
case object KeepAliveMsg extends ImagesMsg