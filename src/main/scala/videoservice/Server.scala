package videoservice

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import httpservices.{SwaggerDocService, VideoService}
import sangria.parser.QueryParser
import sangria.execution.Executor
import sangria.marshalling.sprayJson._
import spray.json._

import scala.util.{Failure, Success}

object Server extends App {
  implicit val system = ActorSystem("sangria-server")
  implicit val materializer = ActorMaterializer()

  import system.dispatcher

  val executor = Executor(
    schema = SchemaDefinition.VideoSchema)

  final val videoRegion: ActorRef = ClusterSharding(Server.system).start(
    typeName = "VideoAggregate",
    entityProps = Props[VideoAggregate],
    settings = ClusterShardingSettings(Server.system),
    extractEntityId = VideoAggregate.extractEntityId,
    extractShardId = VideoAggregate.extractShardId)

  final val videoView = new VideoESView()

  import videoservice.model.VideoJsonProtocol._

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

  val videoRoutes = new VideoService().routes
  val swaggerRoute = new SwaggerDocService(system).routes

  val route = videoRoutes ~ graphQLRoute ~ swaggerRoute

  Http().bindAndHandle(route, "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
