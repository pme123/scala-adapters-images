package pme123.adapters.images.server.boundary

import javax.inject._

import akka.NotUsed
import akka.pattern.ask
import akka.actor.ActorRef
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import controllers.AssetsFinder
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import pme123.adapters.images.server.control.ImagesRegisterParentActor.Create
import pme123.adapters.server.control.http.SameOriginCheck
import pme123.adapters.server.entity.AdaptersContext.settings.httpContext
import pme123.adapters.shared.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-images
  */
@Singleton
class ExampleController @Inject()(@Named("imagesHandlerActor") imagesClientActor: ActorRef
                                  , @Named("imagesParentActor") imagesParentActor: ActorRef
                                  , template: views.html.index
                                  , assetsFinder: AssetsFinder
                                  , cc: ControllerComponents
                                  , val config: Configuration)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc)
    with SameOriginCheck
    with Logger {

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    val context = if (httpContext.length > 1)
      httpContext
    else
      ""
    // uses the AssetsFinder API
    Ok(template(context, assetsFinder))
  }


  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def images(nrOfImages: Int): WebSocket =
    WebSocket.acceptOrResult[JsValue, JsValue] {
      case rh if sameOriginCheck(rh) =>
        wsFutureFlow(Create(rh.id.toString, imagesClientActor)).map { flow =>
          Right(flow)
        }.recover {
          case e: Exception =>
            error(e, "Cannot create websocket")
            val jsError = Json.obj("error" -> "Cannot create websocket")
            val result = InternalServerError(jsError)
            Left(result)
        }

      case rejected =>
        error(s"Request $rejected failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }

  /**
    * Creates a Future containing a Flow of JsValue in and out.
    */
  private def wsFutureFlow(create: Create): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    implicit val timeout: Timeout = Timeout(1.second) // the first run in dev can take a while :-(
    val future: Future[Any] = imagesParentActor ? create
    val futureFlow: Future[Flow[JsValue, JsValue, NotUsed]] = future.mapTo[Flow[JsValue, JsValue, NotUsed]]
    futureFlow
  }

}

