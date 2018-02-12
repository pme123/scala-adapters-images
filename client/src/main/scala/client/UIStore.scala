package client

import com.thoughtworks.binding.Binding
import com.thoughtworks.binding.Binding.Vars
import org.scalajs.dom.raw.HTMLElement
import pme123.adapters.shared.Logger
import shared._

trait UIStore extends Logger {

  protected def uiState: UIState

  protected def changeImageDataList(imageDataList: ImageDataList) {
    info(s"UIStore: changeImageDataList ${imageDataList.dataList}")
    uiState.imageDataList.value.append(imageDataList.dataList.map(ImageElem(_)): _*)
  }

  protected def addImageData(imageData: ImageData) {
    info(s"UIStore: addImageData $imageData")
    uiState.imageDataList.value.append(ImageElem(imageData))
  }


  implicit def makeIntellijHappy(x: scala.xml.Elem): Binding[HTMLElement] = ???

}

case class UIState(imageDataList: Vars[ImageElem] = Vars())
