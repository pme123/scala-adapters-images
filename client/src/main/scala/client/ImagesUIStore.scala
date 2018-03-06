package client

import com.thoughtworks.binding.Binding.Vars
import play.api.libs.json._
import pme123.adapters.client.ToConcreteResults.{ConcreteResult, toConcreteResults}
import pme123.adapters.shared.Logger
import shared.ImageData

import scala.language.implicitConversions

trait ImagesUIStore extends Logger {

  protected def imagesUIState: ImagesUIState

  // type class instance for ImageElem
  implicit object concreteResultForImageElem extends ConcreteResult[ImageElem] {

    override def fromJson(lastResult: JsValue): JsResult[ImageElem] =
      Json.fromJson[ImageData](lastResult)
        .map(ImageElem.apply)
  }

  def updateConcreteResults(lastResults: Seq[JsValue]): Seq[ImageElem] = {
    toConcreteResults(imagesUIState.imageElems, lastResults)
  }
}


case class ImagesUIState(imageElems: Vars[ImageElem] = Vars())
