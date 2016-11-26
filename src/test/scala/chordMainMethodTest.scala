import akka.actor.{Actor, ActorRef, ActorSystem, Props, _}
import akka.util.Timeout
import org.scalatest.FunSuite
import scala.concurrent.duration._
import akka.pattern.ask
import scala.concurrent.Await

/**
  * Created by kruti on 11/25/2016.
  * This file contains Unit Test cases for testing ChordAlgorithm.scala functions.
  */
class chordMainMethodTest extends FunSuite {

  /* Intitial set of intialization for activating the master actor and create the actors for the nodes required in the system */
  implicit val timeout = Timeout(100 seconds)

  val Master = chordMainMethod.system.actorOf(Props(new ChordMainActor(8,1,1,chordMainMethod.system)), name = "MainActor")
  val futureMaster = Master ? "startProcess"
  println(Await.result(futureMaster,timeout.duration ).asInstanceOf[String]+" instantiating chord simulator")

  chordMainMethod.nodeSpace = ((Math.log10(8.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
  val insertMovieName = "testmovie"
  val insertMovieDetails = "testdetails"

  /* test the ring creation - enter one node into the ring */
  test("testCreateRingWithNode") {
    val response = chordMainMethod.CreateRingWithNode(0)
    assert(response.equals("done"))
  }

  /* test to insert a node in the ring*/
  test("testInsertNodeInRing") {
    val response = chordMainMethod.InsertNodeInRing(3)
    assert(response.equals("done"))
  }

  /* test to delete a node in the ring*/
  test("testDeleteNodeInRing") {
    val response = chordMainMethod.DeleteNodeInRing(0)
    assert(response.equals("done"))
  }

  /* test for inserting a movie */
  test("testLookupAndPut"){

    val response = chordMainMethod.LookupItem(insertMovieName)
    println("response from look up item: "+response)
    if (!(response.equals("not found")))
    {
      val response1 = chordMainMethod.LookupListItem(response.toInt,insertMovieName)
      if(response1.equals("not found")){
        val response2 = chordMainMethod.InsertItem(response.toInt,insertMovieName,insertMovieDetails)
        assert(response2.equals("done"))
      }
    }
    else{
      println("movie already already exists")
    }
  }

  /* delete an item - movie name from the system */
  test("testDeleteKey"){
    val response = chordMainMethod.LookupItem(insertMovieName.trim)
    if (!(response.equals("not found")))
    {
      val response1 = chordMainMethod.LookupListItem(response.toInt,insertMovieName)
      if(!(response1.equals("not found")))
      {
        val response2 = chordMainMethod.DeleteKey(response.toInt, insertMovieName);
        assert(response2.equals("done"))
      }
    }
    else{
      println("movie does not already exists")
    }

  }

}
