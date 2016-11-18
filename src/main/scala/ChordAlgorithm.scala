import akka.actor.{Actor, ActorRef, ActorSystem, Props}

case object NodeJoin

case object NodeLeave

case object FindNodeSuccessor

case object StabilizeRing


class ChordMainActor(TotalNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,ChordActorSys: ActorSystem) extends Actor {

  //var m : Int =0
  var activenodes:Int=0
  var NodeArrayActors:Array[Int] = Array()

  def receive = {

    case "startProcess" => {
      println("In Start Process")
      println("Master Node has been Initiated")
      println("Total nodes in the cloud: " + TotalNodes)

      /* total number of nodes present in the system : 2 ^ m*/
      activenodes= ((Math.log10(TotalNodes.toDouble))/(Math.log10(2.toDouble))).ceil.toInt

      NodeArrayActors = new Array[Int](activenodes)

      println("Node of array actors length: maintaining the hashes for each computer/actor length:"+NodeArrayActors.length)
      println("Total number of active nodes in the cloud: " +activenodes)

      /* on the basis of the total nodes - each node must be created - that is intiated as an actor*/

      for (i<-0 to TotalNodes-1)
      {
        println("Inside for loop")
        val workerNodes = ChordActorSys.actorOf(Props(new CloudNodeActor(activenodes,MinMsgs,MaxMsgs,listFileItems,SimulationDuration,SimluationMark,i,self)),name = "Node_"+i.toString)
        workerNodes ! "intiateEachNode"
      }
      InitializeFingerTable
    }
  }

  def InitializeFingerTable: Unit ={

    println("In finger table initialization. Active nodes in the system : "+activenodes)

  }

}

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class CloudNodeActor(ActiveNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,Index: Int,ChordActorSys:ActorRef) extends Actor {

  //val ringArray = Array.ofDim[Int](ActiveNodes,MinMsgs,MaxMsgs,SimluationMark,SimulationDuration,3)
  val fingerTable = Array.ofDim[Int](ActiveNodes,3)
  var nodeHopsCounter:Int=0

  def receive = {
    case "intiateEachNode"=>{
      println("Initiate each node - Total nodes which is bigger than the active nodes in the cloud")

    }
  }
}

object chordMainMethod {


  def main(args: Array[String]) {
    /* The inputs : the number of users, the */
   /* if (args.length != 2)
   {
      println("Input arguements not entered correctly")
    }*/
    //else
    // Correct Arguements entered.
   // {
      println("Input Arguements: No of User, No of nodes, min and max msgs (by each user), simulation duraion , time mark for simulation, items list,  ")
      /*var noOfUsers = args(0).toInt
      var computerNodes = args(1).toInt
      var minMsgs = args(2).toInt
      var maxMsgs = args(3).toInt
      var listFileItems = args(4).toString()
      var simulationDuration = args(5).toInt
      var simulationMark = args(6).toInt
      var readRequest = args(7).toInt
      var writeRequest = args(8).toInt*/

      var noOfUsers = 2
      var totalNodes = 10
      var minMsgs = 1
      var maxMsgs = 3
      var listFileItems = "Check Items"
      var simulationDuration = 10
      var simulationMark = 2
      var readRequest = 2
      var writeRequest = 1

      var system = ActorSystem("ChordProtocol")

      /*for(countUser <-0 to noOfUsers-1){

      }*/
      val Master = system.actorOf(Props(new ChordMainActor(totalNodes,minMsgs,maxMsgs,listFileItems,
        simulationDuration,simulationMark,system)), name = "User_1")
      Master ! "startProcess"

    //}
  }
}