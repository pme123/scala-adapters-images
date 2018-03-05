package client

import play.api.libs.json.{JsResult, JsValue, Json}
import pme123.adapters.client.ToConcreteResults.ConcreteResult
import pme123.adapters.client._
import pme123.adapters.client.demo.DemoResultClient.{info, warn}
import pme123.adapters.shared._
import shared.{EmojiData, ImageData, PhotoData}

import scala.language.implicitConversions
import scala.scalajs.js.annotation.JSExportTopLevel

object ProjectClient
  extends ClientImplicits {

  @JSExportTopLevel("client.ProjectClient.main")
  def imagePage(context: String, websocketPath: String, clientType: String) {
    info(s"JobCockpitClient $clientType: $context$websocketPath")
    ClientType.fromString(clientType) match {
      case CUSTOM_PAGE =>
        ImagesView(context, websocketPath).create()
      case JOB_PROCESS =>
        JobProcessView(context, websocketPath).create()
      case JOB_RESULTS =>
        JobResultsView(context
          , websocketPath
          , CustomResultsInfos(Seq("Image Url / Emoji", "User")
            ,
            s"""<ul>
                  <li>emojiStr, imgUrl: String, * matches any part. Examples are emojiStr=*0034*, imgUrl=*.jp*</li>
                 </ul>""")
        )(ImageDataForJobResultsRow).create()
      case other => warn(s"Unexpected ClientType: $other")
    }
  }
}

trait ClientImplicits
  extends ClientUtils {

  implicit object ImageDataForJobResultsRow extends ConcreteResult[JobResultsRow] {

    override def fromJson(lastResult: JsValue): JsResult[JobResultsRow] =
      Json.fromJson[ImageData](lastResult)
        .map {
          case EmojiData(emojiStr, user) =>
            Seq(td(emojiStr), td(user))
          case PhotoData(imgUrl, user) =>
            Seq(td(imgUrl), td(user))
        }.map(JobResultsRow)

  }

}