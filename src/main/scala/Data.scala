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

class VideoRepo {
  import VideoRepo._

  def addVideo(video: Video): Video = {
    if (video.id.length == 0)
      throw new Exception("invalid id")
    if (videos.exists(v => v.id == video.id))
      throw new Exception("id already exists")

    videos = video :: videos
    video
  }
  def getVideo(id: Option[String]): Option[Video] = id.fold(videos.headOption)(id => videos.find(v => v.id == id))
}

object VideoRepo {
  var videos = List(
    Video("1", "video1")
  )
}