package videoservice

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.sksamuel.elastic4s.{BulkCompatibleDefinition, ElasticClient}
import com.sksamuel.elastic4s.streams.ReactiveElastic._
import com.sksamuel.elastic4s.streams.RequestBuilder
import org.elasticsearch.common.settings.Settings
import com.sksamuel.elastic4s.ElasticDsl._
import videoservice.model.Video

import scala.concurrent.duration._

/**
  * Created by olivierdeckers on 20/08/16.
  */
class VideoESView()(implicit val system: ActorSystem, implicit val actorMaterializer: ActorMaterializer) {
  import ESView.esClient

  val queries = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](
    LeveldbReadJournal.Identifier)

  // Implicit requestbuilder that transforms a Video to an index update statement
  implicit val builder = new RequestBuilder[Video] {
    import com.sksamuel.elastic4s.ElasticDsl._
    def request(video: Video): BulkCompatibleDefinition =
      update (video.id) in "sangria" / "videos" docAsUpsert (
        "id" -> video.id,
        "name" -> video.name
        )
  }
  val subscriber = esClient.subscriber(flushInterval = Some(1.second))

  // Creates stream of VideoEvents and feeds it into elasticsearch
  // (VideoEvents are tagged with by the TaggingEventAdapter
  queries
    .eventsByTag(TaggingEventAdapter.videoEventTag, 0L)
    .map(envelope => {
      envelope.event match {
        case VideoAdded(id, name) => Video(id, name)
          //TODO VideoDeleted
      }
    })
    .runWith(Sink.fromSubscriber(subscriber))

}

object ESView {
  private val settings: Settings = Settings.builder()
    .put("cluster.name", "elasticsearch_olivierdeckers")
    .build()

  val esClient = ElasticClient.transport(settings, "elasticsearch://127.0.0.1:9300")
}