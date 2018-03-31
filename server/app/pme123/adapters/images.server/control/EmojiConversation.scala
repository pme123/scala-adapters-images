package pme123.adapters.images.server.control

import akka.actor.ActorRef
import pme.bot.control.ChatConversation
import pme123.adapters.shared.Logger

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

  val supportedEmojis: Seq[String] = (0x1F600 to 0x1F642).map(Character.toChars(_).mkString)

}

