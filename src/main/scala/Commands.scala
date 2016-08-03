/**
  * Created by olivierdeckers on 26/07/16.
  */

sealed trait Command

case class AddVideo(id: String, name: String) extends Command
case class DeleteVideo(id: String) extends Command
case class GetVideo(id: String) extends Command