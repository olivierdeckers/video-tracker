import sangria.schema._

import scala.concurrent.Future

/**
 * Defines a GraphQL schema for the current project
 */
object SchemaDefinition {

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
        resolve = (ctx) => ctx.ctx.getVideo(ctx.arg(VideoArg))),
      Field("addVideo", OptionType(Video),
        arguments = AddVideoArgs,
        resolve = ctx => {
          println(AddVideoArgs.map(ctx.arg(_)))
          val v = AddVideoArgs.map(ctx.arg(_)) match {
            case (Some(id: String)) :: (name: String) :: Nil => Some(new Video(id, name))
            case _ => None
          }
          v.fold[Option[Video]] (None) (video => Some(ctx.ctx.addVideo(video)))
        })
    ))

  val Schema = Schema(Query)
}
