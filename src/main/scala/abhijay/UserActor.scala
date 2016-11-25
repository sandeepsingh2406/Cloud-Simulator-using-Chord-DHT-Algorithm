package abhijay

import java.util.Calendar

import akka.actor.Actor

/**
  * Created by avp on 11/24/2016.
  */

case class getMovie(movieName: String)
case class putMovie(movieName: String, movieDetails: String)
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
      var result = scala.io.Source.fromURL(url).mkString;
      println("Result: " + result);
    }

    case putMovie(movieName: String, movieDetails: String) => {
      println("Adding movie: " + movieName);
      var url = "http://localhost:8080/?putMovie=" + movieName + "&MovieDetails=" + movieDetails;
      var result = scala.io.Source.fromURL(url).mkString;
      println("Result: " + result);
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
}
