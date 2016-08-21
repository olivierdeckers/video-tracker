package videoservice

import java.util.UUID

import akka.pattern.ask
import akka.util.Timeout
import sangria.schema._
import videoservice.model.Video

import scala.concurrent.duration._
import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

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
    "Query", fields[Unit, Unit](
      Field("video", OptionType(Video),
        arguments = VideoArg :: Nil,
        resolve = (ctx) => {
          //TODO return all videos if not specified
          ctx.arg(VideoArg).fold[Future[Option[Video]]](Future.successful(None)) { id =>
            (Server.videoRegion ? GetVideo(id)).asInstanceOf[Future[Option[Video]]]
          }
        }),
      Field("addVideo", OptionType(Video),
        arguments = AddVideoArgs,
        resolve = ctx => {
          println(AddVideoArgs.map(ctx.arg(_)))

          //TODO extract this to implicit mapper?
          val command = AddVideoArgs.map(ctx.arg(_)) match {
            case List(Some(id: String), name: String) => Some(AddVideo(id, name))
            case List(None, name: String) => Some(AddVideo(UUID.randomUUID().toString, name))
            case _ => None
          }
          command.map { command =>
            Server.videoRegion ! command
            new Video(command.id, command.name)
          }
        })
    ))

  val VideoSchema = Schema(Query)
}
