import java.net.InetAddress
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn


/**
  * Created by singsand on 11/19/2016.
  */
//Object which creates instance of class contain the main code
object mainHandlerService {
  def main(args: Array[String]): Unit = {



    val inst: Service = new Service()
    inst.method(new Array[String](5))
  }
}

class Service(){


  var listOfNodes=new ListBuffer[Integer]()
//  val MasterActor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
  def method(args: Array[String]): Unit = {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    object Route1 {
      val route =
        path("") {
          akka.http.scaladsl.server.Directives.get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Welcome to the Web Service</h1>"))
          }
        }
    }

    object Route2 {
      val route =
        parameters('getMovie) { (moviename) =>
          //output goes here
          println("getMovie")
          complete(s"getMovie")
        }
    }



    object Route3 {
      val route =
        parameters('putMovie) { (moviename) =>
          //output goes here
          println("putMovie")
          complete(s"putMovie")
        }
    }

    object Route4 {
      val route =
        parameters('insertNode) { (nodeId) =>

          println("insertNode, list size:"+listOfNodes.size)

          if(listOfNodes.size==0)
            {

              println("listOfNodes.size==0")
              //create ring called
              val abc=chordMainMethod.putActor(nodeId.toInt)
              println("Back from ring :"+abc)
              listOfNodes += nodeId.toInt

              println("Sending to web "+nodeId)

              complete(s"Ring started with Node "+ nodeId)
            }
          else{
            if(listOfNodes.contains(nodeId))
            {
              complete(s"Node "+ nodeId+"already exists in the ring.")
            }
            else
              {

                //joinring called

                listOfNodes += nodeId.toInt
                complete(s"Added Node "+ nodeId+" to the ring.")


              }

          }

        }
    }

    //specify different handlers(called routes here) for our web service
    object MainRouter {
      val routes =  Route2.route ~ Route3.route ~ Route4.route ~ Route1.route
    }

    val localhost = InetAddress.getLocalHost
    val bindingFuture = akka.http.scaladsl.Http().bindAndHandle(MainRouter.routes, "0.0.0.0", 8080)
    println(s"Check Server online at http://"+"localhost"+":8080/\nPress RETURN to stop...")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}
