import akka.actor.Props
import akka.util.Timeout
import org.scalatest.FunSuite
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask

/**
  * Created by kruti on 11/25/2016.
  */
class chordIntegrationTest extends FunSuite {

  val thread = new Thread(new Runnable {
    def run() {

      //Creating object of SearchEngine1 class to start the web service
      val inst: Service = new Service()
      inst.method(new Array[String](5))
    }
  })

  implicit val timeout = Timeout(100 seconds)
  chordMainMethod.totalNodes = 8
  chordMainMethod.nodeSpace = ((Math.log10(8.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt

  val Master = chordMainMethod.system.actorOf(Props(new ChordMainActor(8,1,1,chordMainMethod.system)), name = "MainActor")
  val futureMaster = Master ? "startProcess"
  println(Await.result(futureMaster,timeout.duration ).asInstanceOf[String]+" instantiating chord simulator")


  //Starting the web service
  thread.start()
  println(s"Waiting 10 seconds for web service to start in another thread. Some output from other class might be visible here.")

  Thread.sleep(10000)
  println(s"Web service has been started in another thread.")

  test("test to create chord system, start a web service and check input output"){

    println("This test case instantiates the ring and the web service to interact with the cloud nodes")

    println("Now calling web service at http://localhost:8080 with parameters insertNode=0 and checking response.")

    var url = "http://localhost:8080/?insertNode=0"
    var result = scala.io.Source.fromURL(url).mkString
    //Get response from web service
    //Stop the web service
    thread.interrupt()
    assert(result.equals("Ring started with Node 0"))

  }
  test("test to insert a movie in a node"){

    println("This test is use to test the insertion of movie at existing nodes in the ring")

    println("Now calling web service at http://localhost:8080 with parameters putMovie=abc&movieDetails=abcddetails and checking response.")

    var url = "http://localhost:8080/?putMovie=testMovie&movieDetails=testmoviedetails"
    var result = scala.io.Source.fromURL(url).mkString

    //Get response from web service
    //Stop the web service
    thread.interrupt()
    assert(result.equals("Movie <testMovie> inserted at Node 0"))


  }

}
