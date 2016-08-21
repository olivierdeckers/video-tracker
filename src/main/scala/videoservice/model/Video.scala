package videoservice.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import io.swagger.annotations.{ApiModel, ApiModelProperty}
import spray.json._

//@ApiModel(description = "a video")
case class Video(
//    @ApiModelProperty("UUID identifier")
    id: String,
//    @ApiModelProperty("The video name")
    name: String)

object VideoJsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val videoFormat = jsonFormat2(Video)
}