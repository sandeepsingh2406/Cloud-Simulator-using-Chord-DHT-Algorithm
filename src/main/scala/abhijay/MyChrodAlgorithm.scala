package abhijay

/**
  * Created by avp on 11/20/2016.
  */

import java.security.MessageDigest

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

case class ActivateNodeInRing(nodeIndex:Int)

case object NodeLeave

case object FindNodeSuccessor

case object StabilizeRing

case class JoinNode(nodeArrayActors:Array[String],nodeIndex:Int)

case class PrintFingerTable(nodeIndex : Int)

case class UpdateSuccessor(nextNodeActorIndex : Int)


class ChordMainActor(TotalNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,ChordActorSys: ActorSystem) extends Actor {

  var activenodes: Int = 0
  var NodeArrayActors  = Array.ofDim[String](TotalNodes,2)
  var m:Int = 0

  def receive = {

    case "startProcess" => {
      println("In Start Process")
      println("Master Node has been Initiated")
      println("Total nodes in the cloud: " + TotalNodes)

      /* total number of nodes present in the system : 2 ^ m*/
      activenodes = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
      m = activenodes

      //NodeArrayActors = Array.ofDim[String](TotalNodes,2)

      println("Node of array actors length: maintaining the hashes for each computer/actor length:" + NodeArrayActors.length)
      println("Total number of active nodes in the cloud: " + activenodes)

      /* on the basis of the total nodes - each node must be created - that is intiated as an actor*/

      InitializeNodeHashTable

      for (i <- 0 to TotalNodes - 1) {
        println("Inside for loop - instantiating actors for each computer in cloud with node: "+i)
        val workerNodes = ChordActorSys.actorOf(Props(new CloudNodeActor(TotalNodes, activenodes, MinMsgs, MaxMsgs, listFileItems, SimulationDuration, SimluationMark, i, self)), name = "node_" + i.toString)
        workerNodes ! "intiateEachNode"
      }
      self ! ActivateNodeInRing(0)

    }

    /* assign the nodes in ring*/
    case ActivateNodeInRing(nodeIndex : Int)=> {
      println("Activate the the node in ring with index: "+nodeIndex)
      println("Node at index: "+nodeIndex+" hashed value: "+NodeArrayActors(nodeIndex)(0).toString)

      /* use the akka actor selection to call each actor as intiated for totoal nodes */
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
      NodeArrayActors(nodeIndex)(1) = "1" //assuming this node is joined - making this active
      eachnodeactor ! JoinNode(NodeArrayActors(nodeIndex),nodeIndex)
    }

    case "CheckHashedNodesStatus" =>{
      var checkAcitveNodes : Array[String] = new Array(TotalNodes)
      checkAcitveNodes = getActiveNode()
      for(i<- 0 to activenodes-1){
        println("Hashed node active: "+checkAcitveNodes(i))
      }
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
      NodeArrayActors(count)(0) = hashValues
      NodeArrayActors(count)(1) = "0"

    }
    /*scala.util.Sorting.quickSort(NodeArrayActors)*/
    /* Print Sort the calculated hashes */
    for (count <- 0 to TotalNodes - 1) {
      println("Sorted Hashed Node at Index: "+count+" key: "+NodeArrayActors(count)(0))
    }
    /* activate the first node in the ring */

  }

  def getHashedValueForNode(node:Int): String ={
    NodeArrayActors(node)(0)
  }

  def getActiveNode(): Array[String] ={
    var activeNodeHashes : Array[String] = new Array(activenodes)
    for(i<-0 to activenodes-1){
      if(NodeArrayActors(i)(1) == "1"){
        activeNodeHashes(i) = i.toString
      }
    }
    activeNodeHashes
  }
}

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class CloudNodeActor(TotalNodes:Int, ActiveNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,Index: Int,ChordActorSys:ActorRef) extends Actor {

  var nodeHopsCounter:Int=0
  val m: Int = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
  val fingerTable = Array.ofDim[Int](m,2)
  implicit val timeout = Timeout(60 seconds)

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
        fingerTable(i)(1) = -1
        /*if(i!=m-1)
        {
          fingerTable(i)(1) = (Index+math.pow(2,i+1).toInt)%TotalNodes;
        }*/

      }

      println("Get Successor for node: "+nodeIndex)
      getSuccessor(nodeIndex)
      Stabilize(nodeIndex)

      //Checks if all the active nodes are added to the network. If not adds the remaining nodes,
      // else if all the active nodes are added then starts the process for message passing.
      if(nodeIndex <= (ActiveNodes-1)) {
        println("When all active nodes are not yet a part of ring,call the master node again to add the active node to the ring")
        sender ! ActivateNodeInRing(nodeIndex + 1)
      }
      else{
        /*println("Now starting web service which helps to interact with cloud system")
        val inst: Service = new Service()
        inst.method(new Array[String](5))
*/
        sender ! "CheckHashedNodesStatus"

        /*println("Finger table updated for all active nodes as:")

        val future = self ? "fetchFingerTable"

        val result = Await.result(future, timeout.duration).asInstanceOf[String]*/

      }
      /*else
      {
        Master ! "startScheduling"
      }*/
    }

    case UpdateSuccessor(nextNodeActorIndex : Int) =>{
      for(i<-0 to (m-1)){
        val tempFingerSucc = fingerTable(i)(0)
        var tempPred = find_predecessor(tempFingerSucc,nextNodeActorIndex)
      }
    }

    case PrintFingerTable(nodeIndex:Int) => {
      for(i<-0 to (fingerTable.length-1)){
        println("Node: "+nodeIndex+" Finger table values at index: "+i+" is: "+fingerTable(i)(0))
      }
      sender ! "done"
    }

    case "fetchFingerTable" => {

      for(i <- 0 to ActiveNodes-1){
        val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+i.toString)
        eachnodeactor ? PrintFingerTable(i)
      }

      println("Done from fetch fingers")
      sender ! "done"
    }

  }
  def getSuccessor(currActorNodeIndex : Int): Unit ={

    println("Inside get successor for node: "+currActorNodeIndex)
    for(i <- 0 to m-1){
      val tempFingerNode = fingerTable(i)(0)
      println("Call find_successor for  node: "+currActorNodeIndex+" with finger table node: "+i+" and value :"+tempFingerNode)
      val getSucc = find_successor(tempFingerNode,currActorNodeIndex)
      println("New successor received as: "+getSucc)
      fingerTable(i)(1) = getSucc
    }
  }

  def find_successor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int = {
    println("Inside Find successor. Call find_predecessor for node: "+currActorNodeIndex+" with finger table start value: "+fingerNodeValue)
    val newSucc = find_predecessor(fingerNodeValue,currActorNodeIndex)
    return newSucc //need to send the newSucc.successor - call the finger table of this new succ node
  }

  def find_predecessor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int ={

    println("Inside find predecssor")
    var tempCurrNode = currActorNodeIndex
    val tempSucc = fingerTable(0)(1)
    println("find predecssor : Finger table : Successor for node: "+currActorNodeIndex+" value: of finger[1].node= "+tempSucc)
    println("find predecssor : Comparing: fingerNodeValue: "+fingerNodeValue+" in range of tempCurrNode= "+tempCurrNode+" and <= tempSucc= "+tempSucc)
    while(fingerNodeValue > tempCurrNode && fingerNodeValue <= tempSucc){
      println("find predecssor : inside while. Call closes preceding finger")
      tempCurrNode = closest_preceding_finger(fingerNodeValue,currActorNodeIndex)
    }
    println("Returning from find predecessor with value: "+tempCurrNode)
    return tempCurrNode
  }

  def closest_preceding_finger(fingerNodeVale : Int, currActorNodeIndex : Int):Int={
    println("inside closest preceding finger : ")
    var count:Int = m-1
    while(count >=0 ){
      println("Inside while with count :"+count)
      println("Comparing : "+fingerTable(count)(1)+" > "+currActorNodeIndex+" && "+fingerTable(count)(1)+" < "+fingerNodeVale)
      if(fingerTable(count)(1) > currActorNodeIndex && fingerTable(count)(1) < fingerNodeVale){
        println("returning: "+fingerTable(count)(1))
        return fingerTable(count)(1)
      }

      count = count - 1
    }
    println("else returning : "+currActorNodeIndex)
    return currActorNodeIndex
  }

  def Stabilize(currActorNodeIndex:Int)
  {
    for(i<-0 to currActorNodeIndex-1)
    {
      //println("notifying node: "+notifyNode.toString)
      val node = context.actorSelection("akka://ChordProtocolHW4/user/node_"+currActorNodeIndex.toString)
      node ! UpdateSuccessor(currActorNodeIndex+1)
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