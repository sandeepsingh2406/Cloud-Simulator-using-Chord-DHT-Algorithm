package abhijay

import akka.actor.{ActorSystem, Props}

/**
  * Created by avp on 11/20/2016.
  */

object MyChordDriver {


  def main(args: Array[String]) {
    /* The inputs : the number of users, the */
    if (args.length == 9) {
      println("Input arguements not entered correctly");
/*
      var noOfUsers = args(0).toInt
      var computerNodes = args(1).toInt
      var minMsgs = args(2).toInt
      var maxMsgs = args(3).toInt
      var listFileItems = args(4).toString()
      var simulationDuration = args(5).toInt
      var simulationMark = args(6).toInt
      var abhijay.readRequest = args(7).toInt
      var abhijay.writeRequest = args(8).toInt
*/
    }
    else {
      //Correct Arguements entered.
      println("Input Arguements: No of User, No of nodes, min and max msgs (by each user), simulation duraion , time mark for simulation, items list,  ")

      var noOfUsers = 2
      var totalNodes = 10
      var minMsgs = 1
      var maxMsgs = 3
      var listFileItems = "Check Items"
      var simulationDuration = 10
      var simulationMark = 2
      var readRequest = 2
      var writeRequest = 1

      val system = ActorSystem("ChordProtocolHW4")

      /*for(countUser <-0 to noOfUsers-1){

    }*/
      val Master = system.actorOf(Props(new MyChordMainActor(totalNodes, minMsgs, maxMsgs, listFileItems,
        simulationDuration, simulationMark, system)), name = "User_1")
      Master ! "startProcess"

      //}
    }
  }
}