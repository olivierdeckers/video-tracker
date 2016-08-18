import javax.ws.rs.Path

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers._
import akka.http.scaladsl.model.StatusCodes._
import akka.util.Timeout

import scala.concurrent.duration._
import akka.pattern.ask
import spray.json._
import akka.http.scaladsl.server.Directives._
import io.swagger.annotations._

import scala.concurrent.ExecutionContext

/**
  * Created by olivierdeckers on 18/08/16.
  */
@Api(value="videos")
@Path("/videos")
class VideoService()(implicit executionContext: ExecutionContext) {

  import VideoJsonProtocol._
  import scala.concurrent.ExecutionContext.Implicits.global

  @ApiOperation(value = "Get video by id", response = classOf[Video], httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "id", required = true, dataType = "string", paramType="path", value="id of the video")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 200, response=classOf[Video], message = ""),
    new ApiResponse(code = 404, message = "Video not found")
  ))
  @Path("/{id}")
  def getVideo = path(Segment) { id =>
    get {
      implicit val timeout = Timeout(1.second)
      complete((Server.videoRegion ? GetVideo(id)).mapTo[Option[Video]].map(_.toJson))
    }
  }

  @ApiOperation(value = "Add video", httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="name", required=true, dataType = "string", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code=201, message = "Video was successfully added")
  ))
  def addVideo =
    post {
      entity(as[Video]) { video =>
        Server.videoRegion ! AddVideo(video.id, video.name)
        complete(ToResponseMarshallable(NoContent))
      }
    }

  def routes = pathPrefix("videos") {
    getVideo ~ addVideo
  }
}
