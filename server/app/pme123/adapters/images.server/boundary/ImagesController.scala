package pme123.adapters.images.server.boundary

import javax.inject._

import controllers.AssetsFinder
import play.api.Configuration
import play.api.mvc._
import pme123.adapters.server.boundary.{AdaptersController, JobCockpitController}

import scala.concurrent.ExecutionContext
/**
  * This class creates the actions and the websocket needed.
  * Original see here: https://github.com/playframework/play-scala-websocket-images
  */
@Singleton
class ImagesController @Inject()(jobController: JobCockpitController
                                 , assetsFinder: AssetsFinder
                                 , cc: ControllerComponents
                                 , val config: Configuration)
                                (implicit val ec: ExecutionContext)
  extends AbstractController(cc)
    with AdaptersController {

  def index: Action[AnyContent] = jobProcess

  def jobProcess: Action[AnyContent] =
      jobController.jobProcess(shared.imagesJobIdent)

  def jobResults: Action[AnyContent] =
    jobController.jobResults(shared.imagesJobIdent)

  def images: Action[AnyContent] =
    jobController.customPage(shared.imagesJobIdent)

}

