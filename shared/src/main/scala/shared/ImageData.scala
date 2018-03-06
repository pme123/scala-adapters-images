package shared

import julienrf.json.derived
import play.api.libs.json.{Json, OFormat}

case class ImageDataList(dataList: List[ImageData] = Nil)

object ImageDataList {
  implicit val jsonFormat: OFormat[ImageDataList] = Json.format[ImageDataList]
}

sealed trait ImageData

object ImageData {
  implicit val jsonFormat: OFormat[ImageData] = derived.oformat[ImageData]()
}

case class EmojiData(emojiStr: String, user: String = "BOT") extends ImageData

case class PhotoData(imgUrl: String, user: String = "BOT") extends ImageData