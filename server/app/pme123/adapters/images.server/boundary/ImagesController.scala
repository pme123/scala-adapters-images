package pme123.adapters.images.server.boundary

import javax.inject._

import akka.actor.ActorRef
import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.{JOB_CLIENT, RESULT_CLIENT, WebsocketController}
import pme123.adapters.server.control.JobActorFactory

import scala.concurrent.ExecutionContext
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-images
  */
@Singleton
class ImagesController @Inject()(val jobFactory: JobActorFactory
                                 , @Named("userParentActor")
                                 val userParentActor: ActorRef
                                 , template: views.html.index
                                 , assetsFinder: AssetsFinder
                                 , cc: ControllerComponents
                                 , val config: Configuration)
                                (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with WebsocketController {

  // Home page that renders template
  def index = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_CLIENT ,assetsFinder))
  }

  def images = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, RESULT_CLIENT, assetsFinder))
  }

}

