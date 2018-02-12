package pme123.adapters.images.server.control

import javax.inject.Inject

import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import akka.stream.Materializer
import pme123.adapters.shared.Logger
import shared._

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.Random

/**
  * This actor handles all the AFClients.
  * During this process it will inform all clients with changed Data.
  */
class ImagesHandlerActor @Inject()()
                                  (implicit val mat: Materializer, val ec: ExecutionContext)
  extends Actor
    with Logger {

  import ImagesHandlerActor._

  private val imgUrls = Seq(
    "https://www.gstatic.com/webp/gallery3/1.png"
    , "https://www.gstatic.com/webp/gallery3/3.png"
    , "https://www.gstatic.com/webp/gallery3/2.png")


  private var photoDataList =
    (for (i <- 0 to nrOfImages)
      yield
        PhotoData(imgUrls(Random.nextInt(3)), "BOT"))
      .toList

  private var emojiDataList =
    ImagesConversation.supportedEmojis
      .map(s => EmojiData(s, "BOT"))
      .toList

  // a map with all clients (Websocket-Actor) that needs the status about the process
  private lazy val actors: mutable.Map[RequestIdent, ActorRef] = mutable.Map()


  // 1. level of abstraction
  // **************************

  def receive = LoggingReceive {
    case SubscribeClient(requestIdent, wsActor) => subscribeAFClient(requestIdent, wsActor)
    case UnSubscribeClient(requestIdent) => unsubscribeAFClient(requestIdent)
    case photoData: PhotoData => changedPhotoData(photoData)
    case emojiData: EmojiData => changedEmojiData(emojiData)
    case other => warn(s"unexpected message: $other")
  }

  // 2. level of abstraction
  // **************************

  // subscribe a user with its id and its websocket-Actor
  // this is called when the websocket for a user is created
  private def subscribeAFClient(requestIdent: RequestIdent, wsActor: ActorRef) {
    info(s"Subscribed AFClient: $requestIdent: $wsActor")
    actors.put(requestIdent, wsActor)
    wsActor ! ImageDataListMsg(ImageDataList(emojiDataList))
  }

  // Unsubscribe a user(remove from the map)
  // this is called when the connection from an AFClient websocket is closed
  private def unsubscribeAFClient(requestIdent: RequestIdent) {
    info(s"Unsubscribe User: $requestIdent")
    actors -= requestIdent
  }

  // called if the adapter process sends new ImageData
  private def changedPhotoData(photoData: PhotoData) {
    photoDataList = (photoData :: photoDataList).take(nrOfImages)
    sendToAFClients(photoData)
  }

  // called if the adapter process sends new ImageData
  private def changedEmojiData(emojiData: EmojiData) {
    emojiDataList = (emojiData :: emojiDataList).take(nrOfImages)
    sendToAFClients(emojiData)
  }

  // 3. level of abstraction
  // **************************

  private def sendToAFClients(imageData: ImageData) {
    actors.values
      .foreach(sendToAFClient(imageData))
  }

  def sendToAFClient(imageData: ImageData)(afClient: ActorRef) {
    afClient ! ImageDataMsg(imageData)
  }

}

object ImagesHandlerActor {

  type RequestIdent = String

  case class SubscribeClient(requestIdent: RequestIdent
                             , registerActor: ActorRef)

  case class UnSubscribeClient(requestIdent: RequestIdent)

  case object GetImages

}
