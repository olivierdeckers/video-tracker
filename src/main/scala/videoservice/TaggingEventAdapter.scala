package videoservice

import akka.persistence.journal.WriteEventAdapter
import akka.persistence.journal.Tagged
import com.sksamuel.elastic4s.streams.ReactiveElastic._

/**
  * Created by olivierdeckers on 20/08/16.
  */
class TaggingEventAdapter extends WriteEventAdapter {

  override def toJournal(event: Any): Any = event match {
    case e: VideoEvent =>
      Tagged(e, Set(TaggingEventAdapter.videoEventTag))
    case _ => event
  }

  override def manifest(event: Any): String = ""
}

object TaggingEventAdapter {
  val videoEventTag = "videoEvent"
}