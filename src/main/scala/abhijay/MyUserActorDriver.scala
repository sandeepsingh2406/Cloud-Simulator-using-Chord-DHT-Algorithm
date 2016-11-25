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
    logger.info(args.deep.mkString(", "));
    if(args.length != 0){
      ParameterConstants.numberOfUsers = args(0).toInt;
      ParameterConstants.minRequests = args(1).toInt;
      ParameterConstants.maxRequests = args(2).toInt;
      ParameterConstants.ratio = args(3);
    }

/*
      block to get max read and write requests from ratio, min and max requests
      e.g. ration = 4:1, minRequests = 0, maxRequests = 13
      divider = 13/(4+1) = 13/5 = 2
*/
      val tokens = ParameterConstants.ratio.split("\\:");
      val readRequest = tokens(0).toInt;
      val writeReqeusts = tokens(1).toInt;
      val totalRequests = readRequest + writeReqeusts;
      val divider = (ParameterConstants.maxRequests / totalRequests).toInt;
      val maxReadRequests = divider * readRequest;
      val maxWriteRequests = divider * writeReqeusts;

    instantiateActors(ParameterConstants.numberOfUsers, actorSystem);
//    val listOfMovies = readFile(ParameterConstants.movieDatabaseFile);
    startSimulation(ParameterConstants.duration, ParameterConstants.numberOfUsers, maxReadRequests, maxWriteRequests, divider);

  }

  def startSimulation(duration: Int, numberOfUsers: Int,
                      maxReadRequests: Int,
                      maxWriteRequests: Int,
                      maxDeleteRequests: Int): Unit ={
    val simulationDuration = duration.seconds.fromNow;
    val random = Random;
    for(i <- 0 until numberOfUsers){
      val id = random.nextInt(numberOfUsers);
      val userNode = actorSystem.actorSelection(ParameterConstants.userActorNamePrefix + ParameterConstants.userNamePrefix + id);
      logger.info("startSimulation() " + userNode.pathString);
      userNode ! writeRequest(0, maxWriteRequests);
      userNode ! readRequest(0, maxReadRequests);
      userNode ! deleteRequest(0, maxDeleteRequests);
      Thread.sleep(1000);
    }
  }

  // add all read movies in cloud simulator
  def addAllMovies(fileName: String): Unit ={

  }

  // read input file and return list of lines
  def readFile(fileName: String) : List[String] = {
    val listOfLines = Source.fromFile(fileName).getLines.toList
    return listOfLines;
  }

  // instantial all user actors
  def instantiateActors(numberOfActors: Int, actorSystem: ActorSystem): Unit = {
    for(i <- 0 until numberOfActors){
      var user = actorSystem.actorOf(Props(new UserActor(i)), name = ParameterConstants.userNamePrefix+i);
    }
  }
}
