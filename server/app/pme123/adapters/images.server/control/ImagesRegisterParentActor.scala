package pme123.adapters.images.server.control

import javax.inject.Inject

import akka.actor._
import akka.event.LoggingReceive
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl._
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.JsValue
import pme123.adapters.images.server.control.ImagesRegisterActor.CreateHandlerActor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
 * Provide some DI and configuration sugar for new ImagesRegisterActor instances.
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  */
class ImagesRegisterParentActor @Inject()(childFactory: ImagesRegisterActor.Factory,
                                          configuration: Configuration)
                                         (implicit ec: ExecutionContext)
  extends Actor with InjectedActorSupport with ActorLogging {
  import ImagesRegisterParentActor._
  implicit private val timeout: Timeout = Timeout(2.seconds)

  override def receive: Receive = LoggingReceive {
    case Create(requestIdent, afHandlerActor) =>
      log.info(s"Creating actor for  $requestIdent")
      val child: ActorRef = injectedChild(childFactory(requestIdent, afHandlerActor), requestIdent)
      val future = (child ? CreateHandlerActor(requestIdent))
        .mapTo[Flow[JsValue, JsValue, _]]
      pipe(future) to sender()
  }
}

object ImagesRegisterParentActor {
  type RequestIdent = String

  case class Create(requestIdent: RequestIdent, afHandlerActor: ActorRef)

}



