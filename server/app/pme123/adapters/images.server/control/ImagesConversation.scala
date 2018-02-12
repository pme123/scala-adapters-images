package pme123.adapters.images.server.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import pme.bot.control.ChatConversation
import pme.bot.entity.SubscrType.SubscrConversation
import pme.bot.entity.{Command, FSMState, Subscription}
import shared.{EmojiData, PhotoData}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
// @formatter:off
/**
  * Add Photos or Emojis depending on the mode.
  *
  *     [Idle]
  *       |----------------
  *       v               |
  *   [AddEmojis]<-       v
  *       |       | [AddPhotos]<-
  *       ---------     |       |
  *                     ---------
  */
// @formatter:on
case class ImagesConversation(handlerActor: ActorRef, mode: ImagesConversation.MODE)(implicit ec: ExecutionContext)
  extends ChatConversation { // this is a Service
  import ImagesConversation._

  when(Idle) {
    case Event(Command(msg, _), _) if mode == EMOJI =>
      bot.sendMessage(msg, "Send an Emoji!")
      goto(AddEmojis)
    case Event(Command(msg, _), _) =>
      bot.sendMessage(msg, "Upload a Photo!")
      goto(AddPhotos)
    case other =>
      notExpectedData(other)
  }

  when(AddEmojis) {
    case Event(Command(msg, _), _) =>
      val emoji = msg.text.get
      if (supportedEmojis.contains(emoji)) {
        bot.sendMessage(msg, s"Thanks, just send another Emoji if you like")
        handlerActor ! EmojiData(emoji, msg.from.map(_.firstName).getOrElse("Unknown"))
      } else
        bot.sendMessage(msg, s"Sorry I expected a Photo or an Emoji")
      stay()
    case other =>
      notExpectedData(other)
  }

  when(AddPhotos) {
    case Event(Command(msg, _), _) =>
      bot.getFilePath(msg)
        .onComplete {
          case Success((_, fileUrl)) =>
            handlerActor ! PhotoData(fileUrl, msg.from.map(_.firstName).getOrElse("Unknown"))
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

  case object AddEmojis extends FSMState

}

object ImagesConversation {
  // the command to listen for
  val addEmojis = "/addemojis"
  val addPhotos = "/addphotos"

  val supportedEmojis: Seq[String] = (0x1F600 to 0x1F642).map(Character.toChars(_).mkString)

  sealed trait MODE

  case object PHOTO extends MODE

  case object EMOJI extends MODE

  // constructor of the Service - which is an Actor
  def props(handlerActor: ActorRef, mode: MODE)(implicit ec: ExecutionContext): Props = Props(ImagesConversation(handlerActor, mode))
}

// a singleton will inject all needed dependencies and subscribe the service
@Singleton
class ImagesConversationSubscription @Inject()(@Named("commandDispatcher") val commandDispatcher: ActorRef
                                               , @Named("imagesHandlerActor") val handlerActor: ActorRef
                                               , val system: ActorSystem)
                                              (implicit ec: ExecutionContext) {

  import ImagesConversation._

  // subscribe the ImagesConversation to the CommandDispatcher
  commandDispatcher ! Subscription(addEmojis, SubscrConversation
    , Some(_ => system.actorOf(props(handlerActor, EMOJI))))

  commandDispatcher ! Subscription(addPhotos, SubscrConversation
    , Some(_ => system.actorOf(props(handlerActor, PHOTO))))

}
