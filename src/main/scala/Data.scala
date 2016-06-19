
case class Video(id: String, name: String)

class VideoRepo {
  import VideoRepo._

  def addVideo(video: Video): Video = {
    if (video.id.length == 0)
      throw new Exception("invalid id")
    if (videos.find(v => v.id == video.id) != None)
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