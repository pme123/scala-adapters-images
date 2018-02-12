package client

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object ProjectClient {

  @JSExportTopLevel("client.ProjectClient.imagesPage")
  def afClientPage(context:String) {
    ImagesClient(context)
      .create()
  }
}