package videoservice

/**
  * Created by olivierdeckers on 26/07/16.
  */

sealed trait Command

trait VideoCommand extends Command {
  val id: String
}
case class AddVideo(override val id: String, name: String) extends VideoCommand
case class DeleteVideo(id: String) extends VideoCommand
case class GetVideo(id: String) extends VideoCommand

