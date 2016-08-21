package videoservice

import akka.persistence.PersistentActor
import akka.actor._
import akka.cluster.sharding.ShardRegion
import videoservice.model.Video

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

  override def persistenceId: String = "videoProcessor-" ++ self.path.name

  override def receiveRecover: Receive = {
    case e: VideoEvent =>
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

  def updateState(event: VideoEvent): Unit = {
    event match {
      case VideoAdded(id, name) =>
        val video = Video(id, name)
        state = Some(video)
      case VideoDeleted(id) =>
        state = None
    }
  }
}

sealed trait VideoEvent
case class VideoAdded(id: String, name: String) extends VideoEvent
case class VideoDeleted(id: String) extends VideoEvent
