import java.util.Calendar

import akka.actor.Actor

/**
  * Created by singsand on 11/19/2016.
  */

case class readRequest(min: Int, max: Int)
case class writeRequest(min: Int, max: Int)



class userActors extends Actor {
  def getStartTime(): Int={

    val startTime=Calendar.getInstance.getTimeInMillis
    return((startTime/1000).toInt)
  }

  def receive = {
    case readRequest(min, max) =>
      val startTime = getStartTime
      print(startTime)

    case writeRequest(min, max) =>
      val startTime = getStartTime
      print(startTime)

  }
}








