package abhijay

import akka.actor.{ActorSystem, Props}
import grizzled.slf4j.Logger

import scala.concurrent.duration._
import scala.io.Source
import scala.util.Random

/**
  * Created by avp on 11/24/2016.
  */

object MyUserActorDriver {

  val actorSystem = ActorSystem(ParameterConstants.userActorName);
  val logger = Logger("Simulation started");

  def main(args: Array[String]): Unit = {

    val user = actorSystem.actorOf(Props(new UserActor(0)), name = ParameterConstants.userNamePrefix+0);
    println(user.path);
//    user ! putMovieFileAndCloud("new", "testdetails", ParameterConstants.movieDatabaseFile);
//    user ! getMovie ("test");

    instantiateActors(ParameterConstants.numberOfUsers, actorSystem);
//    val listOfMovies = readFile(ParameterConstants.movieDatabaseFile);
    startSimulation(ParameterConstants.duration, ParameterConstants.numberOfUsers);

  }

  def startSimulation(duration: Int, numberOfUsers: Int): Unit ={
    val simulationDuration = duration.seconds.fromNow;
    val random = Random;
    for(i <- 0 until numberOfUsers){
      val id = random.nextInt(numberOfUsers);
      val userNode = actorSystem.actorSelection(ParameterConstants.userActorNamePrefix + ParameterConstants.userNamePrefix + id);
      logger.info("startSimulation() " + userNode.pathString);
      userNode ! writeRequest(0, 15);
      userNode ! readRequest(0, 10);
      userNode ! deleteRequest(0, 5);
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
