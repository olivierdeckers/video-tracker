import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import org.scalatest._

/**
  * Created by olivierdeckers on 08/08/16.
  */
// TODO experiment with Asyncflatspeclike?
class VideoAggregateSpec extends TestKit(ActorSystem("spec")) with fixture.FlatSpecLike with BeforeAndAfterAll with ImplicitSender with DefaultTimeout {

  "A VideoAggregate" should "create a video" in { tuple =>
    val videoAggregate = tuple._2
    videoAggregate ! AddVideo(tuple._1, "test")
    videoAggregate ! GetVideo(tuple._1)
    expectMsg(Some(Video(tuple._1, "test")))
  }

  it should "return none if video doesn't exist" in { tuple =>
    tuple._2 ! GetVideo(tuple._1)
    expectMsg(None)
  }

  it should "Recreate state from events when restarted" in { tuple =>
    val id = tuple._1
    var videoAggregate = tuple._2
    videoAggregate ! AddVideo(id, "test")

    // Check message was processed
    videoAggregate ! GetVideo(id)
    expectMsg(Some(Video(id, "test")))

    val testProbe = TestProbe()
    testProbe watch videoAggregate
    system.stop(videoAggregate)
    testProbe.expectTerminated(videoAggregate)

    videoAggregate = system.actorOf(Props[VideoAggregate], s"videoAggregate-$id")
    videoAggregate ! GetVideo(id)
    expectMsg(Some(Video(id, "test")))
  }

  override def withFixture(test: OneArgTest): Outcome = {
    val id = UUID.randomUUID().toString
    val videoAggregate = system.actorOf(Props[VideoAggregate], s"videoAggregate-$id")
    test((id, videoAggregate))
  }

  override type FixtureParam = (String, ActorRef)

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
