package pme123.adapters.images.server.control

import javax.inject._

import akka.actor._
import akka.event.{LogMarker, MarkerLoggingAdapter}
import akka.stream._
import akka.stream.scaladsl._
import akka.util.Timeout
import akka.{Done, NotUsed}
import com.google.inject.assistedinject.Assisted
import play.api.libs.json._
import pme123.adapters.images.server.control.ImagesHandlerActor.{SubscribeClient, UnSubscribeClient}
import pme123.adapters.images.server.control.ImagesRegisterParentActor.RequestIdent
import shared.{ImagesMsg, KeepAliveMsg}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Creates a initiator actor that sets up the websocket stream.  Although it's not required,
  * having an actor manage the stream helps with lifecycle and monitoring, and also helps
  * with dependency injection through the AkkaGuiceSupport trait.
  *
  * Original see here: https://github.com/playframework/play-scala-websocket-example
  *
  */
class ImagesRegisterActor @Inject()(@Assisted requestIdent: RequestIdent
                                    , @Assisted afHandlerActor: ActorRef)
                                   (implicit mat: Materializer, ec: ExecutionContext)
  extends Actor {

  import ImagesRegisterActor._

  // Useful way to mark out individual actors with websocket request context information...
  private val marker = LogMarker(name = self.path.name)
  implicit private val log: MarkerLoggingAdapter = akka.event.Logging.withMarker(context.system, this.getClass)
  implicit private val timeout: Timeout = Timeout(50.millis)

  /**
    * The receive block, useful if other actors want to manipulate the flow.
    * This is used by the AFRegisterParentActor to initiate the Websocket for a client.
    */
  override def receive: Receive = {
    case CreateHandlerActor(_) =>
      log.info(s"Create Websocket for Client: $requestIdent: $afHandlerActor")
      afHandlerActor ! SubscribeClient(requestIdent, wsActor())
      sender() ! websocketFlow
    case other =>
      log.info(s"Unexpected message from ${sender()}: $other")
  }

  /**
    * If this actor is killed directly, stop anything that we started running explicitly.
    * In our case unsubscribe the client in the AFHandlerActor
    */
  override def postStop(): Unit = {
    log.info(marker, s"Stopping $requestIdent: actor $self")
    afHandlerActor ! UnSubscribeClient(requestIdent)
  }

  /**
    * Generates a flow that can be used by the websocket.
    *
    * @return the flow of JSON
    */
  private lazy val websocketFlow: Flow[JsValue, JsValue, NotUsed] = {
    // Put the source and sink together to make a flow of hub source as output (aggregating all
    // ImagesMsgs as JSON to the browser) and the actor as the sink (receiving any JSON messages
    // from the browse), using a coupled sink and source.
    Flow.fromSinkAndSourceCoupled(jsonSink, hubSource)
      .watchTermination() { (_, termination) =>
        // When the flow shuts down, make sure this actor also stops.
        termination.foreach(_ =>
          if(context != null)
            context.stop(self))
        NotUsed
      }
  }

  private val (hubSink, hubSource) = MergeHub.source[JsValue](perProducerBufferSize = 16)
    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
    .run()

  private val jsonSink: Sink[JsValue, Future[Done]] = Sink.foreach { json =>
    // should not be called for now
    json.validate[ImagesMsg] match {
      case JsSuccess(other, _) =>
        log.warning(marker, s"Unexpected message from ${sender()}: $other")
      case JsError(errors) =>
        log.error(marker, "Other than ImagesMsg: " + errors.toString())
    }
  }

  /**
    * Creates an ActorRef that handles the outgoing ImagesMsg one by one and send them to the hub.
    */
  private def wsActor(): ActorRef = {
    // We convert everything to JsValue so we get a single stream for the websocket.
    // As all messages are EuroCisBotMessages we only need one Source.
    val afRegisterActorSource = Source.actorRef(Int.MaxValue, OverflowStrategy.fail)
    // Set up a complete runnable graph from the adapter source to the hub's sink
    Flow[ImagesMsg]
       // send every minute a KeepAliveMsg - as with akka-http there is an idle-timeout
      .keepAlive(1.minute, () => KeepAliveMsg)
      .map(Json.toJson[ImagesMsg])
      .to(hubSink)
      .runWith(afRegisterActorSource)
  }
}


object ImagesRegisterActor {

  // used to inject a ImagesRegisterActor as child of the ImagesRegisterParentActor
  trait Factory {
    def apply(requestIdent: RequestIdent, handlerActor: ActorRef): Actor
  }

  case class CreateHandlerActor(requestIdent: RequestIdent)

}

