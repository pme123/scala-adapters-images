package pme123.adapters.images.server.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import pme.bot.control.ChatConversation
import pme.bot.entity.SubscrType.SubscrConversation
import pme.bot.entity.{Command, FSMState, Subscription}
import shared.EmojiData

import scala.concurrent.ExecutionContext
// @formatter:off
/**
  * Add Photos or Emojis depending on the mode.
  *
  *     [Idle]
  *       |
  *       v
  *   [AddEmojis]<-
  *       |       |
  *       ---------
  *
  */
// @formatter:on
case class EmojiConversation(imagesRepo: ActorRef)
                            (implicit ec: ExecutionContext)
  extends ChatConversation { // this is a Service
  import EmojiConversation._

  when(Idle) {
    case Event(Command(msg, _), _) =>
      println(s"received Command! $msg")
      bot.sendMessage(msg, "Send an Emoji!")
      goto(AddEmojis)
    case other =>
      notExpectedData(other)
  }

  when(AddEmojis) {
    case Event(Command(msg, _), _) =>
      val emoji = msg.text.get
      if (supportedEmojis.contains(emoji)) {
        bot.sendMessage(msg, s"Thanks, just send another Emoji if you like")
        imagesRepo ! EmojiData(emoji, msg.from.map(_.firstName).getOrElse("Unknown"))
      } else
        bot.sendMessage(msg, s"Sorry I expected a Photo or an Emoji")
      stay()
    case other =>
      notExpectedData(other)
  }

  // state to indicate that the count button is already shown to the User
  case object AddEmojis extends FSMState

}

object EmojiConversation {
  // the command to listen for
  val command = "/addemojis"
  val supportedEmojis: Seq[String] = (0x1F600 to 0x1F642).map(Character.toChars(_).mkString)

  // constructor of the Service - which is an Actor
  def props(imagesRepo: ActorRef)(implicit ec: ExecutionContext): Props = Props(EmojiConversation(imagesRepo))

}

// a singleton will inject all needed dependencies and subscribe the service
@Singleton
class EmojiConversationSubscription @Inject()(@Named("commandDispatcher") commandDispatcher: ActorRef
                                              , @Named("imagesRepo") imagesRepo: ActorRef
                                              , system: ActorSystem)
                                             (implicit ec: ExecutionContext) {

  import EmojiConversation._

  // subscribe the EmojiConversation to the CommandDispatcher
  commandDispatcher ! Subscription(command, SubscrConversation
    , Some(_ => system.actorOf(props(imagesRepo))))

}
