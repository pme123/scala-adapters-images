package pme123.adapters.images.server.control

import akka.actor.{ActorRef, ActorSystem, Props}
import javax.inject.{Inject, Named, Singleton}
import pme.bot.control.ChatConversation
import pme.bot.entity.SubscrType.SubscrConversation
import pme.bot.entity.Subscription

import scala.concurrent.ExecutionContext
// @formatter:off
/**
  * Add Emojis depending on the mode.
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
