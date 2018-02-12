package client

import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom.window
import shared.{EmojiData, ImageData, PhotoData}

import scala.util.Random

sealed trait ImageElem {

  def imageElement: Binding[HTMLElement]

  protected lazy val randomImgStyle: String = {
    val sHeight = window.screen.height.toInt
    val sWidth = window.screen.width.toInt
    val width = Random.nextInt(sWidth / 20) + 100
    val height = Random.nextInt(sHeight / 20) + 100
    s"""
       | position: absolute;
       | top:${Random.nextInt(sHeight - height) + height / 2}px;
       | left:${Random.nextInt(sWidth - width)}px;
       | max-width:${width}px;
       | max-height:${height}px;
       | font-size:${width}px;
       """.stripMargin
  }

}

object ImageElem {
  def apply(imageData: ImageData): ImageElem = imageData match {
    case emojiData: EmojiData=> Emoji(emojiData)
    case photoData: PhotoData=> Photo(photoData)
  }
}

case class Emoji(emojiData: EmojiData) extends ImageElem {

  @dom
  lazy val imageElement: Binding[HTMLElement] =
    <div style={randomImgStyle}>
      {emojiData.emojiStr}
    </div>

}

case class Photo(photoData: PhotoData) extends ImageElem {

  @dom
  lazy val imageElement: Binding[HTMLElement] =
      <img style={randomImgStyle} src={photoData.imgUrl}/>

}
