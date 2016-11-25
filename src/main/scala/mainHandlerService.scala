import java.net.InetAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import java.io.File

import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import grizzled.slf4j.Logger

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

class Service() {


  val logger = Logger("mainHandlerService Class")

  //  val MasterActor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
  def method(args: Array[String]): Unit = {




    logger.info("Starting log")

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
        parameters('getMovie) { (moviename1) =>
          //output goes here
          println("getMovie")

          var moviename = moviename1.replaceAll("^\"|\"$", "");

          logger.info("Received Request to find movie \""+moviename+"\"")

          if (chordMainMethod.ActorJoined.size != 0) {


            logger.info("Looking up node which should contain this movie \""+moviename+"\" as per it's HASH")

            val node_id = chordMainMethod.LookupItem(moviename.trim).toInt

            logger.info("Response: Node which should contain the movie is \""+node_id+"\"")
            logger.info("Checking if node \""+node_id+"\""+" contains the movie item.")


            val response = chordMainMethod.LookupListItem(node_id, moviename.trim);



            if (response.equals("not found")) {
              logger.info("Response: Not found at Node \""+node_id+"\"")
              complete(s"Movie not found")

            }
            else {
              logger.info("Response: Movie found at Node \""+node_id+"\". Movie name, details: "+response)
              complete(s"Movie found at Node " + node_id + ": " + response)
            }}

          else {
            logger.error("Response: Error! Ring does not contain any node currently")
            println("chordMainMethod.ActorJoined.size==0")
            complete(s"Ring does not contain any node currently")
          }
        }
    }


      object Route3 {
        val route =
          parameters('putMovie, 'movieDetails) { (moviename1, moviedetails1) =>
            //output goes here
            println("putMovie")
            var moviename = moviename1.replaceAll("^\"|\"$", "");
            var moviedetails = moviedetails1.replaceAll("^\"|\"$", "");

            logger.info("Received Request to put movie \""+moviename+"\"")

            logger.info("Looking up which node should contain this movie \""+moviename+"\" as per it's HASH")

            if (chordMainMethod.ActorJoined.size != 0) {
              val node_id = chordMainMethod.LookupItem(moviename.trim).toInt

              logger.info("Response: Node which should contain the movie is \""+node_id+"\"")

              println(" Movie should be at " + node_id)
              val response = chordMainMethod.LookupListItem(node_id, moviename.trim);
              println("LookupListItem response " + response)

              logger.info("Storing movie at node \""+node_id+"\"")

              if (response.equals("not found")) {

                println("Inserting ")
                chordMainMethod.InsertItem(node_id, moviename.trim, moviedetails.trim)
                logger.info("Response: Movie "+moviename1+" stored at node \""+node_id+"\"")
                complete(s"Movie <" + moviename + "> inserted at Node " + node_id)
              }
              else {
                logger.error("Response: Error! Movie "+moviename1+" already exists at node \""+node_id+"\"")

                complete(s"Movie already exists")
              }
            }
            else {
              logger.error("Response: Error! Ring does not contain any node currently. Cannot store movie item.")
              println("chordMainMethod.ActorJoined.size==0")
              complete(s"Ring does not contain any node currently")
            }

          }
      }

      object Route4 {
        val route =
          parameters('insertNode) { (nodeId1) =>
            var nodeId = nodeId1.replaceAll("^\"|\"$", "").toInt;
            println("insertNode, list size:" + chordMainMethod.ActorJoined.size)
            logger.info("Request Received: Insert Node " + nodeId)
            logger.info("Attempting to insert node in the ring.")


            if(nodeId>=chordMainMethod.totalNodes)
              {
                logger.error("Response: Error! Node " + nodeId+" is not in limit. Should be between 0 & "+chordMainMethod.totalNodes)
                complete(s"Please enter a value between 0 and "+chordMainMethod.totalNodes)
              }

            else {
              if (chordMainMethod.ActorJoined.size == 0) {

                println("chordMainMethod.ActorJoined.size==0")
                //create ring called
                val abc = chordMainMethod.CreateRingWithNode(nodeId)
                println("Back from ring :" + abc + ", Node: " + nodeId)

                println("Sending to web " + nodeId)

                logger.info("Response: Ring created with first node: " + nodeId + ". Current Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))

                complete(s"Ring started with Node " + nodeId)
              }
              else {
                if (chordMainMethod.ActorJoined.contains(nodeId)) {
                  logger.error("Response: Error! Node " + nodeId + " already exists in the ring")

                  complete(s"Node " + nodeId + " already exists in the ring.")
                }
                else {

                  //joinring called
                  val abc = chordMainMethod.InsertNodeInRing(nodeId.toInt)
                  println("Back from ring join :" + abc + ", Node: " + nodeId)

                  println("Sending to web " + nodeId)

                  logger.info("Added Node " + nodeId + " to the ring. Current Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))
                  complete(s"Added Node " + nodeId + " to the ring.")


                }

              }
            }
            }

          }



      object Route5 {
        val route =
          parameters('nodeLeave) { (nodeId1) =>
            //output goes here
            println("nodeLeave")
            var nodeId = nodeId1.replaceAll("^\"|\"$", "").toInt

            logger.info("Request Received: Remove Node " + nodeId)
            logger.info("Attempting to leave Node " + nodeId)

            if (chordMainMethod.ActorJoined.size == 0) {

              println("chordMainMethod.ActorJoined.size==0")
              logger.error("Response: Error! Ring does not contain any node currently")
              complete(s"Ring does not contain any node currently")
            }
            else if (!chordMainMethod.ActorJoined.contains(nodeId)) {
              logger.error("Response: Error! Ring does not contain node"+ nodeId + "\nCurrent Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))

              complete(s"Ring does not contain node " + nodeId + "\nCurrent Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))
            }
            else {

              //delete method call and key transfer method call
              val abc = chordMainMethod.DeleteNodeInRing(nodeId.toInt)
              println("Back from ring leave:" + abc + ", Node: " + nodeId)
              logger.info("Node " + nodeId + " left from ring. \nCurrent Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))

              complete(s"Node " + nodeId + " left from ring. \nCurrent Nodes: " + chordMainMethod.ActorJoined.sortWith(_ < _))
            }
          }
      }

      object Route6 {
        val route =
          parameters('deleteMovie) { (moviename1) =>
            //output goes here
            //output goes here
            println("deleteMovie")
            var moviename = moviename1.replaceAll("^\"|\"$", "");

            logger.info("Request Received: Delete Movie item " + moviename1)
            logger.info("Looking up node which should contain this movie \""+moviename+"\" as per it's HASH")

            if (chordMainMethod.ActorJoined.size == 0) {

              println("chordMainMethod.ActorJoined.size==0")
              logger.error("Response: Error! Ring does not contain any node currently")
              complete(s"Not deleted. Ring does not contain any node currently")
            }

            val node_id = chordMainMethod.LookupItem(moviename.trim).toInt
            val response = chordMainMethod.LookupListItem(node_id, moviename.trim);

            logger.info("Response: Node which should contain the movie is \""+node_id+"\"")
            logger.info("Checking if node \""+node_id+"\""+" contains the movie item.")

            if (response.equals("not found"))
              {
              logger.error("Response: Error! Not found at Node \""+node_id+"\"")
              complete(s"Movie not found")}
            else {

              val response = chordMainMethod.DeleteKey(node_id, moviename.trim);
              logger.info("Response: Movie found at Node " + node_id + ". Movie deleted: " + moviename)
              complete(s"Movie found at Node " + node_id + ": " + response +
                "\nMovie deleted: " + moviename + ": " + response)
            }

          }
      }


      //specify different handlers(called routes here) for our web service
      object MainRouter {
        val routes = Route2.route ~ Route3.route ~ Route4.route ~ Route5.route ~ Route6.route ~ Route1.route
      }

      val localhost = InetAddress.getLocalHost
      val bindingFuture = akka.http.scaladsl.Http().bindAndHandle(MainRouter.routes, "0.0.0.0", 8080)
      println(s"Check Server online at http://" + "localhost" + ":8080/\nPress RETURN to stop...")

      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done

    }

}


