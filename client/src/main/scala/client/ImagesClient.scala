package client

import com.thoughtworks.binding.Binding.Constants
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.raw.HTMLElement
import slogging.{ConsoleLoggerFactory, LoggerConfig}

import scala.language.implicitConversions

case class ImagesClient(context: String)
  extends UIStore {

  LoggerConfig.factory = ConsoleLoggerFactory()

  val uiState = UIState()

  lazy val socket: ImagesClientWebsocket = ImagesClientWebsocket(uiState, context)

  // 1. level of abstraction
  // **************************

  def create(): Unit = {
    dom.render(document.body, render)
    socket.connectWS()
  }

  @dom
  def render: Binding[HTMLElement] = <div>
    {imageContainer.bind}
  </div>

  // 2. level of abstraction
  // **************************
  @dom
  private def imageContainer = {
    val imageDL: Seq[ImageElem] = uiState.imageDataList.bind
    <div>
      {Constants(imageDL: _*)
      .map(_.imageElement.bind)}
    </div>
  }

}
