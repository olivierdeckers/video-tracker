import akka.persistence.PersistentActor
import akka.actor._
import scala.collection.mutable.LinkedHashMap
import scala.concurrent.duration._

class VideoAggregateManager extends Actor with ActorLogging {

  val aggregates = LinkedHashMap.empty[String, ActorRef]
  val maxAggregates = 1

  println("starting aggregate manager")

  def receive: Receive = {
    case event@AddVideo(id, _) =>
      getAggregate(id).forward(event)
    case event@DeleteVideo(id) =>
      getAggregate(id).forward(event)
    case event@GetVideo(id) =>
      getAggregate(id) forward event
    case e =>
      println("received invalid message: {}", e)
  }

  def getAggregate = (id: String) => {
    aggregates.getOrElse(id, {
      println("creating new videoAggregate", id)
      val actor = context.actorOf(Props(new VideoAggregate(id)))
      aggregates.put(id, actor)
      if (aggregates.size > maxAggregates) {
        println("removing videoAggregate", aggregates.head._1)
        aggregates.remove(aggregates.head._1)
      }
      actor
    })
  }

}

/*
TODO create one processor per video, that loads the video into memory, validates and applies command
Publisher can create materialized view in database that is eventually consistent, but videoprocessor is always consistent
videoprocessor writes snapshot to snapshot store periodically

Since videoprocessor is loaded lazily when request is sent to it, the startup cost of applying the events in memory is not that heavy and comparable to loading it from database
 */
class VideoAggregate(id: String) extends PersistentActor {

  var state: Option[Video] = None

  override def receiveRecover: Receive = {
    case e: Event =>
      println("recover", e)
      updateState(e)
  }

  override def receiveCommand: Receive = {
    case AddVideo(_, name) =>
      val event = VideoAdded(id, name)
      persist(event)(updateState)

    case DeleteVideo(_) =>
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

  override def persistenceId: String = "videoProcessor-" ++ id

}

sealed trait Event
case class VideoAdded(id: String, name: String) extends Event
case class VideoDeleted(id: String) extends Event
