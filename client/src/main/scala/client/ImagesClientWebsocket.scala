package client

import org.scalajs.dom.raw._
import org.scalajs.dom.window
import play.api.libs.json.{JsError, JsSuccess, Json}
import shared.{ImageDataListMsg, ImageDataMsg, ImagesMsg}

import scala.scalajs.js.timers.setTimeout
import shared.nrOfImages

case class ImagesClientWebsocket(uiState: UIState, context: String)
  extends UIStore {

  private val host = s"${window.location.protocol.replace("http", "ws")}"
  private val baseUrl = s"$host//${window.location.host}$context/ws/images/$nrOfImages"

  private lazy val socket: WebSocket = new WebSocket(baseUrl)

  def connectWS() {
    info(s"Connect Websocket on $baseUrl")
    socket.onmessage = {
      (e: MessageEvent) =>
        val message = Json.parse(e.data.toString)
        message.validate[ImagesMsg] match {
          case JsSuccess(ImageDataListMsg(idl), _) =>
            changeImageDataList(idl)
          case JsSuccess(ImageDataMsg(id), _) =>
            addImageData(id)
          case JsSuccess(other, _) =>
            info(s"Other message: $other")
          case JsError(errors) =>
            error(s"failed message: $message")
            errors.foreach(e => error(e.toString))
        }
    }
    socket.onerror = { (e: ErrorEvent) =>
      error(s"exception with websocket: ${e.message}!")
      socket.close(0, e.message)
    }
    socket.onopen = { (_: Event) =>
      info("websocket open!")
    }
    socket.onclose = { (e: CloseEvent) =>
      info("closed socket" + e.reason)
      setTimeout(1000) {
        connectWS() // try to reconnect automatically
      }
    }
  }

}
