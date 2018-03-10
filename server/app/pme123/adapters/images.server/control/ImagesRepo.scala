package pme123.adapters.images.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef}
import akka.stream.Materializer
import play.api.libs.json.Json
import pme123.adapters.images.server.control.EmojiConversation.supportedEmojis
import pme123.adapters.shared._
import shared.{EmojiData, PhotoData}

import scala.concurrent.ExecutionContext

class ImagesRepo @Inject()(implicit val mat: Materializer, val ec: ExecutionContext)
  extends Actor
    with Logger {

  import ImagesRepo._

  private var optJobActor: Option[ActorRef] = None

  private var lastType: ImagesType = PHOTO

  val maxEmojis = 300
  val maxPhotos = 50

  private var emojiDataList =
    supportedEmojis
      .map(s => EmojiData(s))
      .toList

  private var photoDataList =
    (for {
      i <- 2 to 3
      k <- 1 to 5
    } yield PhotoData(s"https://www.gstatic.com/webp/gallery$i/$k.png"))
      .toList


  def receive: PartialFunction[Any, Unit] = {
    case InitRepo(jobActor) => optJobActor = Some(jobActor)
    case SwitchPage => switchPage()
    case data: EmojiData =>
      addEmoji(data)

    case data: PhotoData =>
      addPhoto(data)

    case other => warn(s"unexpected message: $other")
  }

  private def addPhoto(data: PhotoData) {
    photoDataList = photoDataList :+ data
    if (photoDataList.lengthCompare(maxPhotos) > 0)
      photoDataList = photoDataList.drop(1)
    if (lastType == PHOTO)
      optJobActor.foreach(_ ! GenericResult(Json.toJson(data)))
  }

  private def addEmoji(data: EmojiData) {
    emojiDataList = emojiDataList :+ data
    if (emojiDataList.lengthCompare(maxEmojis) > 0)
      emojiDataList = emojiDataList.drop(1)
    if (lastType == EMOJI)
      optJobActor.foreach(_ ! GenericResult(Json.toJson(data)))
  }

  private def switchPage() {
    lastType match {
      case EMOJI =>
        optJobActor.foreach(_ ! GenericResults(photoDataList.map(d => Json.toJson(d))))
        lastType = PHOTO
      case PHOTO =>
        optJobActor.foreach(_ ! GenericResults(emojiDataList.map(d => Json.toJson(d))))
        lastType = EMOJI
    }
    sender() ! lastType
  }


}

object ImagesRepo {

  sealed trait ImagesType

  case object EMOJI extends ImagesType

  case object PHOTO extends ImagesType

  case class InitRepo(jobActor: ActorRef)

  case object SwitchPage

}