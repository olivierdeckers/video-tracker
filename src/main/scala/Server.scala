import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import sangria.parser.QueryParser
import sangria.execution.Executor
import sangria.marshalling.sprayJson._
import spray.json._

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Server extends App {
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val executor = Executor(
    schema = SchemaDefinition.VideoSchema,
    userContext = new VideoRepo)

  val videoRegion: ActorRef = ClusterSharding(Server.system).start(
    typeName = "VideoAggregate",
    entityProps = Props[VideoAggregate],
    settings = ClusterShardingSettings(Server.system),
    extractEntityId = VideoAggregate.extractEntityId,
    extractShardId = VideoAggregate.extractShardId)

  import VideoJsonProtocol._

  val graphQLRoute: Route =
    (post & path("graphql")) {
      entity(as[JsValue]) { requestJson =>
        val JsObject(fields) = requestJson

        val JsString(query) = fields("query")

        val operation = fields.get("operation") collect {
          case JsString(op) => op
        }

        val vars = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case Some(JsString(s)) => s.parseJson
          case _ => JsObject.empty
        }

        QueryParser.parse(query) match {

          // query parsed successfully, time to execute it!
          case Success(queryAst) =>
            complete(executor.execute(queryAst,
              operationName = operation,
              variables = vars))

          // can't parse GraphQL query, return error
          case Failure(error) =>
            complete(BadRequest, JsObject("error" -> JsString(error.getMessage)))
        }
      }
    }

  val videoRoutes =
    path("videos" / Segment) { id =>
      get {
        implicit val timeout = Timeout(1.second)
        complete((videoRegion ? GetVideo(id)).mapTo[Option[Video]].map(_.toJson))
      }
    } ~
    path("videos") {
      post {
        entity(as[Video]) { video =>
          videoRegion ! AddVideo(video.id, video.name)
          complete(ToResponseMarshallable(NoContent))
        }
      }
    }

  val route = videoRoutes ~ graphQLRoute

  Http().bindAndHandle(route, "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
