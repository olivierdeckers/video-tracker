import akka.actor.{ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import sangria.schema._

import scala.concurrent.duration._
import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

//  val videoAggregateManager: ActorRef = Server.system.actorOf(Props[VideoAggregateManager])

  implicit val timeout = Timeout(1.second)

  val Video = ObjectType("Video", "A video", fields[Unit, Video](
    Field("id", StringType, Some("The id of the video"), resolve = _.value.id),
    Field("name", StringType, Some("The name of the video"), resolve = _.value.name)
  ))

  val VideoArg = Argument("id", OptionInputType(StringType), description = "id of the video")

  val AddVideoArgs = List(
    Argument("id", OptionInputType(StringType), description = "id of the new video"),
    Argument("name", StringType, description = "name of the video")
  )

  val Query = ObjectType(
    "Query", fields[VideoRepo, Unit](
      Field("video", OptionType(Video),
        arguments = VideoArg :: Nil,
        resolve = (ctx) => {
          ctx.arg(VideoArg).fold[Future[Option[Video]]](Future.successful(None)) { id =>
            (Server.videoRegion ? GetVideo(id)).asInstanceOf[Future[Option[Video]]]
//            (videoAggregateManager ? GetVideo(id)).asInstanceOf[Future[Option[Video]]]
          }
        }),
      Field("addVideo", OptionType(Video),
        arguments = AddVideoArgs,
        resolve = ctx => {
          println(AddVideoArgs.map(ctx.arg(_)))
          val v = AddVideoArgs.map(ctx.arg(_)) match {
            case (Some(id: String)) :: (name: String) :: Nil => Some(new Video(id, name))
            case _ => None
          }
          v.fold[Option[Video]] (None) (video => {
            Server.videoRegion ! AddVideo(video.id, video.name)
//            videoAggregateManager ! AddVideo(video.id, video.name)
            Some(video)
          })
        })
    ))

  val VideoSchema = Schema(Query)
}
