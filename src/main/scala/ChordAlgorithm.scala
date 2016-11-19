import java.security.MessageDigest

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.Random

case class ActivateNodeInRing(nodeIndex:Int)

case object NodeLeave

case object FindNodeSuccessor

case object StabilizeRing

case class JoinNode(nodeArrayActors:Array[String],nodeIndex:Int)


class ChordMainActor(TotalNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,ChordActorSys: ActorSystem) extends Actor {

  var activenodes: Int = 0
  var NodeArrayActors: Array[String] = Array()
  var m:Int = 0

  def receive = {

    case "startProcess" => {
      println("In Start Process")
      println("Master Node has been Initiated")
      println("Total nodes in the cloud: " + TotalNodes)

      /* total number of nodes present in the system : 2 ^ m*/
      activenodes = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
      m = activenodes

      NodeArrayActors = new Array[String](TotalNodes)

      println("Node of array actors length: maintaining the hashes for each computer/actor length:" + NodeArrayActors.length)
      println("Total number of active nodes in the cloud: " + activenodes)

      /* on the basis of the total nodes - each node must be created - that is intiated as an actor*/

      for (i <- 0 to TotalNodes - 1) {
        println("Inside for loop - instantiating actors for each computer in cloud")
        val workerNodes = ChordActorSys.actorOf(Props(new CloudNodeActor(TotalNodes, activenodes, MinMsgs, MaxMsgs, listFileItems, SimulationDuration, SimluationMark, i, self)), name = "node_" + i.toString)
        workerNodes ! "intiateEachNode"
      }
      InitializeNodeHashTable
    }

    /* assign the nodes in ring*/
    case ActivateNodeInRing(nodeIndex : Int)=> {
      println("Activate the the node in ring with index: "+nodeIndex)
      println("Node at index: "+nodeIndex+" hashed value: "+NodeArrayActors(nodeIndex))

      /* use the akka actor selection to call each actor as intiated for totoal nodes */
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
      eachnodeactor ! JoinNode(NodeArrayActors,nodeIndex)
    }
  }

  def md5(s: String) = { MessageDigest.getInstance("MD5").digest(s.getBytes).toString }

  def InitializeNodeHashTable: Unit = {

    println("In finger table initialization -> for Active nodes in the system : " + activenodes)

    var count:Int = 0
    var randomStr:String = ""
    var hashValues:String =""

    /* udpate the array to store the hashed value for a random generated string for only the active nodes */
    for (count <- 0 to TotalNodes - 1) {
      randomStr = Random.alphanumeric.take(40).mkString
      println("Random String: "+randomStr)
      hashValues = md5(randomStr)
      println("Hash value from MD5 digest: "+hashValues)
      val forHash = Math.pow(2.toDouble,m.toDouble-1).toInt

      hashValues = hashValues.substring(0,forHash)

      println("hashvalue after substring : "+hashValues)

      /**  hashed value for each active node : **/
      NodeArrayActors(count) = hashValues

    }
    scala.util.Sorting.quickSort(NodeArrayActors)
    /* Print Sort the calculated hashes */
    for (count <- 0 to TotalNodes - 1) {
      println("Sorted Hashed Node at Index: "+count+" key: "+NodeArrayActors(count))
    }
    /* activate the first node in the ring */
    self ! ActivateNodeInRing(0)

  }
}

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class CloudNodeActor(TotalNodes:Int, ActiveNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,Index: Int,ChordActorSys:ActorRef) extends Actor {

  //val ringArray = Array.ofDim[Int](ActiveNodes,MinMsgs,MaxMsgs,SimluationMark,SimulationDuration,3)
  val fingerTable = Array.ofDim[Int](ActiveNodes,2)
  var nodeHopsCounter:Int=0
  val m: Int = ActiveNodes

  def receive = {
    case "intiateEachNode" => {
      println("Initiate Node")
    }

    case JoinNode(nodeArrayActors:Array[String], nodeIndex:Int) => {
      println("In Join node for each actor reperesenting the active node in ring - now joining ring")
      for(i<-0 to m-1)
      {
        println("Value inserted at index: "+i+" is: "+((Index+math.pow(2,i).toInt)%TotalNodes))
        /* calculate the node that the current node actor*/
        fingerTable(i)(0) = (Index+math.pow(2,i).toInt)%TotalNodes
        /*if(i!=m-1)
        {
          fingerTable(i)(1) = (Index+math.pow(2,i+1).toInt)%TotalNodes;
        }*/

      }
      //getSucc(actNodeArray,index)
      //notify(actNodeArray,index)
      //Checks if all the active nodes are added to the network. If not adds the remaining nodes,
      // else if all the active nodes are added then starts the process for message passing.
      if(nodeIndex < ActiveNodes-1) {
        println("When all active nodes are not yet a part of ring,call the master node again to add the active node to the ring")
        sender ! ActivateNodeInRing(nodeIndex + 1)
      }
      else{
        println("Finger table updated for all active nodes as:")
        for(i<-0 to fingerTable.length-1){
          println("Value at index: "+i+" is: "+fingerTable(i)(0))
        }
        println("Now starting web service which helps to interact with cloud system")
        val inst: Service = new Service()
        inst.method(new Array[String](5))

      }
      /*else
      {
        Master ! "startScheduling"
      }*/
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

      val system = ActorSystem("ChordProtocolHW4")

      /*for(countUser <-0 to noOfUsers-1){

      }*/
      val Master = system.actorOf(Props(new ChordMainActor(totalNodes,minMsgs,maxMsgs,listFileItems,
        simulationDuration,simulationMark,system)), name = "User_1")
      Master ! "startProcess"

    //}
  }
}