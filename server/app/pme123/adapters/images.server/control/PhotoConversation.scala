package pme123.adapters.images.server.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import play.api.libs.json.{JsValue, Json}
import pme.bot.control.ChatConversation
import pme.bot.entity.SubscrType.SubscrConversation
import pme.bot.entity.{Command, FSMState, Subscription}
import pme123.adapters.server.control.JobActorFactory
import pme123.adapters.shared.{GenericResult, GenericResults}
import shared.PhotoData

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
// @formatter:off
/**
  * Add Photos or Emojis depending on the mode.
  *
  *     [Idle]
  *       |
  *       v
  *   [AddPhotos]<-
  *       |       |
  *       ---------
  */
// @formatter:on
case class PhotoConversation(handlerActor: ActorRef
                            )
                            (implicit ec: ExecutionContext)
  extends ChatConversation { // this is a Service
  import PhotoConversation._

  when(Idle) {
    case Event(Command(msg, _), _) =>
      bot.sendMessage(msg, "Upload a Photo!")
      handlerActor ! GenericResults(photoDataList)
      goto(AddPhotos)
    case other =>
      notExpectedData(other)
  }

  when(AddPhotos) {
    case Event(Command(msg, _), _) =>
      bot.getFilePath(msg)
        .onComplete {
          case Success((_, fileUrl)) =>
            handlerActor ! GenericResult(Json.toJson(PhotoData(fileUrl, msg.from.map(_.firstName).getOrElse("Unknown"))))
            bot.sendMessage(msg, s"Thanks, just add another image if you like")
            self ! ExecutionResult(AddPhotos, NoData)
          case Failure(exc: Throwable) =>
            warn(s"Unexpected message instead of Photo: ${exc.getMessage}")
            bot.sendMessage(msg, s"Sorry I expected a Photo or an Emoji")
            self ! ExecutionResult(AddPhotos, NoData)
        }
      goto(WaitingForExecution)
    case other =>
      notExpectedData(other)
  }

  // state to indicate that the count button is already shown to the User
  case object AddPhotos extends FSMState

}

object PhotoConversation {
  // the command to listen for
  val command = "/addphotos"

  // constructor of the Service - which is an Actor
  def props(handlerActor: ActorRef)(implicit ec: ExecutionContext): Props = Props(PhotoConversation(handlerActor))

  // TestData
  lazy val photoDataList: List[JsValue] =
    (for {
      i <- 2 to 3
      k <- 1 to 5
    } yield PhotoData(s"https://www.gstatic.com/webp/gallery$i/$k.png"))
      .map(d => Json.toJson(d))
      .toList

}

// a singleton will inject all needed dependencies and subscribe the service
@Singleton
class PhotoConversationSubscription @Inject()(@Named("commandDispatcher") val commandDispatcher: ActorRef
                                              , val jobFactory: JobActorFactory
                                              , val system: ActorSystem)
                                             (implicit ec: ExecutionContext) {

  import PhotoConversation._

  commandDispatcher ! Subscription(command, SubscrConversation
    , Some(_ => system.actorOf(props(jobFactory.jobActorFor(shared.imagesJobIdent)))))

}
