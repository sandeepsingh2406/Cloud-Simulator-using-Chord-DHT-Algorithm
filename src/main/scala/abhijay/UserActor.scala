package abhijay

import java.io.{FileWriter, IOException, PrintWriter}
import java.util.Calendar

import akka.actor.Actor
import grizzled.slf4j.Logger

import scala.util.Random

/**
  * Created by avp on 11/24/2016.
  */

case class getMovie(movieName: String)
case class putMovieFileAndCloud(movieName: String, movieDetails: String, movieDatabaseFile: String)
case class putMovieCloud(movieName: String, movieDetails: String)
case class deleteMovie(movieName: String)
case class readRequest(min: Int, max: Int)
case class writeRequest(min: Int, max: Int)

class UserActor(userId: Int) extends Actor {

  val logger = Logger("UserActor" + userId);
  val URL = "http://127.0.0.1:8080/";

  def getStartTime(): Int={

    val startTime=Calendar.getInstance.getTimeInMillis
    return((startTime/1000).toInt)
  }

  def receive = {

    case getMovie(movieName) => {
      var url = URL + "?getMovie=" + movieName;
      var result = getURLContent(url);
      logger.info("User" + userId + "; getMovie Result: " + result);
    }

    case putMovieFileAndCloud(movieName, movieDetails, movieDatabaseFile) => {
      logger.info("Adding movie: " + movieName);
      var url = URL + "?putMovie=" + movieName + "&movieDetails=" + movieDetails;
      var result = getURLContent(url);
      logger.info("User" + userId + "; putMovie Result: " + result);
      if(movieDatabaseFile != null){
        appendFile(movieDatabaseFile, movieName + ":" + movieDetails);
      }
    }

    case putMovieCloud(movieName, movieDetails) => {
      self ! putMovieFileAndCloud(movieName, movieDetails, null);
    }

    case deleteMovie(movieName) => {

    }

    case readRequest(min, max) => {
      val random = Random;
      val startTime = getStartTime;
      logger.info(startTime);
      val listOfMovies = MyUserActorDriver.readFile(ParameterConstants.movieDatabaseFile);
      val numberOfMovies = listOfMovies.length;
      for(i <- min until max+1){
        val id = random.nextInt(numberOfMovies);
        self ! getMovie(listOfMovies(id).split("\\@")(0));
        Thread.sleep(((60/max).ceil.toLong)*1000);
      }
    }

    case writeRequest(min, max) => {
      val random = Random;
      val startTime = getStartTime;
      logger.info("case: writeRequest; startTime=" + startTime);
      val listOfMovies = MyUserActorDriver.readFile(ParameterConstants.movieDatabaseFile);
      val numberOfMovies = listOfMovies.length;
      for(i <- min until max+1){
        val id = random.nextInt(numberOfMovies);
        val movieTokens = listOfMovies(id).split("\\@");
        logger.info("Adding movie: " + movieTokens(0));
        putMovieMethod(movieTokens(0), movieTokens(1));
        Thread.sleep(((60/max).ceil.toLong)*1000);
      }
    }
  }

  // retrieve contents from URL
  def getURLContent(url: String) : String = {
    var result: String = "";
    try{
      result = scala.io.Source.fromURL(url).mkString;
    }
    catch{
      case ioe: IOException =>{
        logger.info("IOException, ignoring");
      }
      case _: Throwable => {
        logger.info("Exception, ignoring");
      }
    }
    return result;
  }

  // write movie details to file
  @throws (classOf[IOException])
  def appendFile(fileName: String, data: String): Unit ={
    try{
      logger.info("writing to file " + data);
      val fileWriter = new FileWriter(fileName, true);
      fileWriter.write( "\n" + data);
      fileWriter.close();
    }
    catch{
      case ioe: IOException =>{
        logger.info("IOException: can't write to file " + fileName);
      }
      case _: Throwable => {
        logger.info("Exception: can't write to file " + fileName);
      }
    }
  }

  def putMovieMethod(movieName: String, movieDetails:String): Unit ={
    var url = URL + "?putMovie=" + movieName + "&movieDetails=" + movieDetails;
    var result = getURLContent(url);
    logger.info("User" + userId + "; putMovie Result: " + result);
  }
}
