import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import pme.bot.boundary.BotRunner
import pme.bot.control.CommandDispatcher
import pme123.adapters.images.server.control._
import pme123.adapters.server.control.JobActorFactory
import slogging.{LoggerConfig, SLF4JLoggerFactory}

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    bind(classOf[JobActorFactory]).to(classOf[ImagesJobFactory])

    // framework
    LoggerConfig.factory = SLF4JLoggerFactory()
    // Generic for the play-akka-telegrambot4s library
    // the generic CommandDispatcher
    bindActor[CommandDispatcher]("commandDispatcher")
    // starts the Bot itself (Boundary)
    bind(classOf[BotRunner]).asEagerSingleton()

    // your Services:
    bind(classOf[EmojiConversationSubscription]).asEagerSingleton()
    bind(classOf[PhotoConversationSubscription]).asEagerSingleton()

  }
}
