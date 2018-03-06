package client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.client.{AdaptersClient, ClientWebsocket}
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions

case class ImagesView(context: String, websocketPath: String)
  extends AdaptersClient
    with ImagesUIStore {

  val imagesUIState = ImagesUIState()

  LoggerConfig.factory = ConsoleLoggerFactory()


  private lazy val socket = ClientWebsocket(uiState, context)

  // 1. level of abstraction
  // **************************

  @dom
  def render: Binding[HTMLElement] = {
    socket.connectWS(Some(websocketPath))
    <div>
    {imageContainer.bind}
  </div>
  }

  // 2. level of abstraction
  // **************************
  @dom
  private def imageContainer = {
    val demoResults = uiState.lastResults.bind
    <div>
      {Constants(updateConcreteResults(demoResults): _*)
      .map(_.imageElement.bind)}
    </div>
  }

}
