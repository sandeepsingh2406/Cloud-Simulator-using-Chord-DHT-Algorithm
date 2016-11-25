package abhijay

import java.io.{FileWriter, IOException, PrintWriter}
import java.util.Calendar

import akka.actor.Actor

/**
  * Created by avp on 11/24/2016.
  */

case class getMovie(movieName: String)
case class putMovie(movieName: String, movieDetails: String, movieDatabaseFile: String)
case class deleteMovie(movieName: String)
case class readRequest(min: Int, max: Int)
case class writeRequest(min: Int, max: Int)

class UserActor(userId: Int) extends Actor {
  def getStartTime(): Int={

    val startTime=Calendar.getInstance.getTimeInMillis
    return((startTime/1000).toInt)
  }

  def receive = {

    case getMovie(movieName: String) => {
      println("getMovie: " + movieName);
      var url = "http://localhost:8080/?getMovie=" + movieName;
      var result = getURLContent(url);
      println("Result: " + result);
    }

    case putMovie(movieName: String, movieDetails: String, movieDatabaseFile: String) => {
      println("Adding movie: " + movieName);
      var url = "http://localhost:8080/?putMovie=" + movieName + "&MovieDetails=" + movieDetails;
      var result = getURLContent(url);
      println("Result: " + result);
      appendFile(movieDatabaseFile, movieName + ": " + movieDetails);
    }

    case deleteMovie(movieName: String) => {

    }

    case readRequest(min, max) => {
      val startTime = getStartTime
      print(startTime)
    }

    case writeRequest(min, max) => {
      val startTime = getStartTime
      print(startTime)
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
        println("IOException, ignoring");
      }
      case _: Throwable => {
        println("Exception, ignoring");
      }
    }
    return result;
  }

  // write movie details to file
  @throws (classOf[IOException])
  def appendFile(fileName: String, data: String): Unit ={
    try{
      val fileWriter = new FileWriter(fileName, true);
      fileWriter.write(data);
      fileWriter.close();
    }
    catch{
      case ioe: IOException =>{
        println("IOException: can't write to file " + fileName);
      }
      case _: Throwable => {
        println("Exception: can't write to file " + fileName);
      }
    }
  }
}
