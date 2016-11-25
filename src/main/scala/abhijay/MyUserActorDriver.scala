package abhijay

import akka.actor.{ActorSystem, Props}
import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

/**
  * Created by avp on 11/24/2016.
  */

object MyUserActorDriver {

  val actorSystem = ActorSystem(ParameterConstants.userActorName);

  def main(args: Array[String]): Unit = {

    val user = actorSystem.actorOf(Props(new UserActor(0)), name = ParameterConstants.userNamePrefix+0);
    println(user.path);
    user ! putMovieFileAndCloud("new", "testdetails", ParameterConstants.movieDatabaseFile);
    user ! getMovie ("test");
    user ! writeRequest(0, 15);
    user ! readRequest(0, 10);

    instantiateActors(ParameterConstants.numberOfUsers, actorSystem);
    val listOfMovies = readFile(ParameterConstants.movieDatabaseFile);
//    println(listOfMovies(2).split("\\:")(0) + ", " + listOfMovies(2).split("\\:")(1));
//    startSimulation(ParameterConstants.duration, ParameterConstants.numberOfUsers);

  }

  def startSimulation(duration: Int, numberOfUsers: Int): Unit ={
    val simulationDuration = duration.seconds.fromNow;
    val random = Random;
    for(i <- 0 until numberOfUsers){
      val id = random.nextInt(ParameterConstants.numberOfUsers);
//      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
      val node = actorSystem.actorSelection(ParameterConstants.userActorNamePrefix + ParameterConstants.userNamePrefix + id);
      println(node.pathString);
      Thread.sleep(1000);
    }
  }

  def addAllMovies(fileName: String): Unit ={

  }

  def readFile(fileName: String) : List[String] = {
    val listOfLines = Source.fromFile(fileName).getLines.toList
    return listOfLines;
  }

  def instantiateActors(numberOfActors: Int, actorSystem: ActorSystem): Unit = {
    for(i <- 1 until numberOfActors){
      var user = actorSystem.actorOf(Props(new UserActor(i)), name = ParameterConstants.userNamePrefix+i);
    }
  }
}
