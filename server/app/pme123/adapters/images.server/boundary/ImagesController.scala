package pme123.adapters.images.server.boundary

import javax.inject._

import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.AdaptersController
import pme123.adapters.shared.{CUSTOM_PAGE, JOB_PROCESS, JOB_RESULTS}

import scala.concurrent.ExecutionContext
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-images
  */
@Singleton
class ImagesController @Inject()(template: views.html.index
                                 , assetsFinder: AssetsFinder
                                 , cc: ControllerComponents
                                 , val config: Configuration)
                                (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  val websocketPath = s"/${shared.imagesJobIdent}"

  def index: Action[AnyContent] = jobProcess

  def jobProcess: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_PROCESS
      , websocketPath
      , assetsFinder))
  }

  def jobResults: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, JOB_RESULTS
      , websocketPath
      , assetsFinder))
  }

  def images: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    // uses the AssetsFinder API
    Ok(template(context, CUSTOM_PAGE
      , websocketPath
      , assetsFinder))
  }

}

