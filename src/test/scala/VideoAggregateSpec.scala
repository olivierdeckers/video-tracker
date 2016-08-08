import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{FlatSpec, FlatSpecLike}

/**
  * Created by olivierdeckers on 08/08/16.
  */
class VideoAggregateSpec extends TestKit(ActorSystem("spec")) with FlatSpecLike {

  "A VideoAggregate" should "create a video" in {
    val videoAggregate = TestActorRef[VideoAggregate](Props(new VideoAggregate("1")))
    videoAggregate ! AddVideo("1", "test")

    awaitAssert(videoAggregate.underlyingActor.state === Some(Video("1", "test")))
  }
}
