import akka.persistence.PersistentActor
import akka.actor._
import akka.cluster.sharding.ShardRegion

object VideoAggregate {

  def props = Props[VideoAggregate]

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case command: VideoCommand => (command.id, command)
  }

  val numberOfShards = 10
  val extractShardId: ShardRegion.ExtractShardId = {
    case command: VideoCommand => (command.id.hashCode % numberOfShards).toString
  }
}


class VideoAggregate extends PersistentActor {

  var state: Option[Video] = None

  override def receiveRecover: Receive = {
    case e: Event =>
      println("recover", e)
      updateState(e)
  }

  override def receiveCommand: Receive = {
    case AddVideo(id, name) =>
      val event = VideoAdded(id, name)
      persist(event)(updateState)

    case DeleteVideo(id) =>
      val event = VideoDeleted(id)
      persist(event)(updateState)

    case GetVideo(_) =>
      sender() ! state
  }

  def updateState(event: Event): Unit = {
    println("updating state")
    event match {
      case VideoAdded(id, name) =>
        val video = Video(id, name)
        state = Some(video)
        // TODO this should be done in a separate actor: either a PersistentView or look at akka-persistent-query
        VideoRepo.videos = video :: VideoRepo.videos
      case VideoDeleted(id) =>
        state = None
        VideoRepo.videos = VideoRepo.videos.filter(v => v.id != id)
    }
  }

  override def persistenceId: String = "videoProcessor-" ++ self.path.name

  println(persistenceId)

}

sealed trait Event
case class VideoAdded(id: String, name: String) extends Event
case class VideoDeleted(id: String) extends Event
