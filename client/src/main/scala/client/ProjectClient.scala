package client

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object ProjectClient {

  @JSExportTopLevel("client.ProjectClient.main")
  def imagePage(context:String) {
    ImagesClient(context)
      .create()
  }
}