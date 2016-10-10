package videoservice.httpservices

import akka.pattern.ask
import javax.ws.rs.Path
import videoservice.model.Video
import videoservice.model.VideoJsonProtocol._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import io.swagger.annotations._
import videoservice.{AddVideo, GetVideo, Server}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import sangria.marshalling.sprayJson._
import spray.json._

/**
  * Created by olivierdeckers on 18/08/16.
  */
@Api(value="videos")
@Path("/videos")
class VideoService()(implicit executionContext: ExecutionContext) {
  import videoservice.ESView.esClient

  implicit object VideoHitAs extends HitAs[Video] {
    override def as(hit: RichSearchHit): Video = {
      Video(hit.sourceAsMap("id").toString, hit.sourceAsMap("name").toString)
    }
  }

  @ApiOperation(value = "Search videos", response=classOf[Video], responseContainer="List", httpMethod = "GET")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name="query", required=false, dataType = "string", paramType = "query")
  ))
  @ApiResponses(Array(
    new ApiResponse(code=200, response=classOf[Video], responseContainer="List", message = "Returns list of matching videos")
  ))
  def getVideos = (get & parameters('query.?)) { q: Option[String] =>
    complete {
      import com.sksamuel.elastic4s.ElasticDsl._

      val query = search in "sangria" / "videos"
      q foreach { q => query.query(q) }

      val resp = esClient.execute(query)
      resp.map(hits => hits.as[Video]).map(_.toJson)
    }
  }

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
    getVideo ~ getVideos ~ addVideo
  }
}
