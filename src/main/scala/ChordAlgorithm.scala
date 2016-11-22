import java.security.MessageDigest

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global


case class JoinNode(prevNode: Int, newNode: Int)

case class ActivateNodeInRing(nodeIndex:Int)

case class ActivateOtherNode(prevIndex : Int,nodeIndex : Int)

case object NodeLeave

case object FindNodeSuccessor

case object StabilizeRing

case class CreateRing(nodeArrayActors:Array[String],nodeIndex:Int)

case class PrintFingerTable(nodeIndex : Int)

case class UpdateSuccessor(nextNodeActorIndex : Int)

case class GetPredecessor(tempSucc:Int)

case class GetSuccessor(tempNode: Int)

case class NotifyNode(notifyThisNode : Int, currentCallingNode : Int)

case class GetClosesNodes(fingerNodeValue : Int,tempCurrNode : Int)


class ChordMainActor(TotalNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,ChordActorSys: ActorSystem) extends Actor {

  var activenodes: Int = 0
  var NodeArrayActors  = Array.ofDim[String](TotalNodes)
  var m:Int = 0

  def receive = {

    case "startProcess" => {
      println("In Start Process")
      println("Master Node has been Initiated")
      println("Total nodes in the cloud: " + TotalNodes)

      /* total number of nodes present in the system : 2 ^ m*/
      activenodes = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
      m = activenodes

      println("Node of array actors length: maintaining the hashes for each computer/actor length:" + NodeArrayActors.length)
      println("Total number of active nodes in the cloud: " + activenodes)

      /* on the basis of the total nodes - each node must be created - that is intiated as an actor*/

      InitializeNodeHashTable

      for (i <- 0 until TotalNodes) {
        println("Inside for loop - instantiating actors for each computer in cloud with node: "+i)
        val workerNodes = ChordActorSys.actorOf(Props(new CloudNodeActor(TotalNodes, activenodes, MinMsgs, MaxMsgs, listFileItems, SimulationDuration, SimluationMark, i, self)), name = "node_" + i.toString)
        workerNodes ! "intiateEachNode"
      }
      self ! ActivateNodeInRing(0)

    }

    /* assign the nodes in ring*/
    case ActivateNodeInRing(nodeIndex : Int)=> {
      println("Activate the the node in ring with index: "+nodeIndex)
      println("Node at index: "+nodeIndex+" hashed value: "+NodeArrayActors(nodeIndex).toString)

      /* use the akka actor selection to call each actor as intiated for totoal nodes */
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)

      eachnodeactor ! CreateRing(NodeArrayActors,nodeIndex)
    }

    case ActivateOtherNode(prevIndex : Int, nodeIndex : Int) => {

      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)

      eachnodeactor ! JoinNode(prevIndex,nodeIndex)
    }

  }

  def md5(s: String) = { MessageDigest.getInstance("MD5").digest(s.getBytes).toString }

  def InitializeNodeHashTable: Unit = {

    println("In finger table initialization -> for Active nodes in the system : " + activenodes)

    var count:Int = 0
    var randomStr:String = ""
    var hashValues:String =""

    /* udpate the array to store the hashed value for a random generated string for only the active nodes */
    for (count <- 0 until TotalNodes) {
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
    /*scala.util.Sorting.quickSort(NodeArrayActors)*/
    /* Print Sort the calculated hashes */
    for (count <- 0 until TotalNodes) {
      println("Sorted Hashed Node at Index: "+count+" key: "+NodeArrayActors(count))
    }
    /* activate the first node in the ring */

  }

  def getHashedValueForNode(node:Int): String ={
    NodeArrayActors(node)
  }

}

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class CloudNodeActor(TotalNodes:Int, ActiveNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
                     SimluationMark : Int,Index: Int,ChordActorSys:ActorRef) extends Actor {

  var nodeHopsCounter:Int=0
  val m: Int = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
  val fingerTable = Array.ofDim[Int](m,2)
  implicit val timeout = Timeout(20 seconds)
  var predecessor :Int = -1
  var successor : Int = -1
  var next_finger: Int = 1
  var isActiveNode : Int = -1

  def receive = {
    case "intiateEachNode" => {
      println("Initiate Node")
    }

    case JoinNode(prevNode: Int, newNode: Int) => {
      println("In JoinNode for node: "+newNode+" with previous node: "+prevNode)
    }

    case CreateRing(nodeArrayActors:Array[String], nodeIndex:Int) => {
      isActiveNode = 1
      successor = nodeIndex

      println("In create ring for first node: "+nodeIndex)
      for(i<-0 until m)
      {
        println("Value inserted at index: "+i+" is: "+((Index+math.pow(2,i).toInt)%TotalNodes))
        /* calculate the node that the current node actor*/
        fingerTable(i)(0) = (Index+math.pow(2,i).toInt)%TotalNodes
      }

      println("locate Successor for node: "+nodeIndex)
      locate_successor(nodeIndex)
      if(nodeIndex != 0)
        Stabilize(nodeIndex)

      // fix_fingers(nodeIndex)

      //Checks if all the active nodes are added to the network. If not adds the remaining nodes,
      // else if all the active nodes are added then starts the process for message passing.
      if(nodeIndex <= (ActiveNodes-1)) {
        println("When all active nodes are not yet a part of ring,call the master node again to add the active node to the ring")
        sender ! ActivateOtherNode(nodeIndex,nodeIndex + 1)
      }
      else{
        /*println("Now starting web service which helps to interact with cloud system")
        val inst: Service = new Service()
        inst.method(new Array[String](5))*/

        println("Finger table updated for all active nodes as:")

        //self ! "fetchFingerTable"

      }
    }

    case PrintFingerTable(nodeIndex:Int) => {
      for(i<-0 to (fingerTable.length-1)){
        println("Node: "+nodeIndex+" Finger table values at index: "+i+" is: "+fingerTable(i)(0)+" successor: "+fingerTable(i)(1))
      }
      sender ! "done"
    }

    case "fetchFingerTable" => {

      for(i <- 0 to 1){
        val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+i.toString)
        eachnodeactor ! PrintFingerTable(i)
      }

      println("Done from fetch fingers")
      sender ! "done"
    }

    case GetPredecessor(tempSucc:Int) =>{
      println("Get Predecessor for node: "+tempSucc+" value: "+this.predecessor)
      sender ! this.predecessor
    }

    case GetSuccessor(tempNode: Int) =>{
      println("Get Successor for node: "+tempNode+" value: "+this.successor)
      sender ! this.successor

    }

    case NotifyNode(notifyThisNode : Int, currentCallingNode : Int) => {
      println("In notify node for notifying: "+notifyThisNode+" with calling node index: "+currentCallingNode)


      if(this.predecessor == -1 ||

        ((this.predecessor > currentCallingNode && (notifyThisNode > this.predecessor || notifyThisNode < currentCallingNode)) ||
          (this.predecessor < currentCallingNode && notifyThisNode > this.predecessor && notifyThisNode < currentCallingNode)
          || this.predecessor == currentCallingNode && notifyThisNode != this.predecessor ))

      {
        //transfer keys

        this.predecessor = notifyThisNode;
      }


      println("New predecessor for node: "+currentCallingNode+" value is: "+this.predecessor)
    }

    case GetClosesNodes(fingerNodeValue : Int,tempCurrNode : Int) => {

      val tempNode = closest_preceding_finger(fingerNodeValue,tempCurrNode)
      sender ! tempNode
    }

  }


  def locate_successor(currActorNodeIndex : Int): Unit ={

    println("Inside locate successor for node: "+currActorNodeIndex)
    for(i <- 0 until m){
      val tempFingerNode = fingerTable(i)(0)
      println("Call find_successor for  node: "+currActorNodeIndex+" with finger table node: "+i+" and value :"+tempFingerNode)
      val getSucc = find_successor(tempFingerNode,currActorNodeIndex)
      println("New successor received as: "+getSucc)
      fingerTable(i)(1) = getSucc
    }
  }

  def find_successor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int = {
    println("Inside Find successor. Call find_predecessor for node: "+currActorNodeIndex+" with finger table start value: "+fingerNodeValue)

    var fetchRes : Int = -1

    val newSucc = find_predecessor(fingerNodeValue,currActorNodeIndex)

    if(currActorNodeIndex == newSucc){
      println("Current node same as prev")
      fetchRes = this.successor
    }
    else {
      val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + newSucc.toString)


      val futureSucc = tempActor ? GetSuccessor(newSucc)

      fetchRes = Await.result(futureSucc, timeout.duration).asInstanceOf[Int]
    }

    //tempActor.forward(GetSuccessor(newSucc))
    //tempActor ! GetSuccessor(newSucc)
//    val result : Future[Int] = tempActor.ask(GetSuccessor(newSucc))(100 seconds).mapTo[Int]
//    result.onSuccess {
//      case output: Int => {
//        fetchRes = output
//        //fingerTable(fingerIndex)(1) = fetchRes
//        //println("find_succ => fetch result from on success of "+currActorNodeIndex +" value in finger table: at node: "+fingerIndex+" value as: " +fetchRes)
//      }
//    }

    /*while(fetchRes == -1) {
      println("find_succe - thread.sleep")
      //wait(100)
    }*/
     println("find_succ : after on success: "+fetchRes)

    return fetchRes
  }

  def find_predecessor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int ={

    println("Inside find predecssor")
    var tempCurrNode_dash : Int = 0
    var tempCurrNode = currActorNodeIndex
    var tempSucc = this.successor
    println("find predecssor : Finger table : Successor for node: "+currActorNodeIndex+" value: of finger[1].node= "+tempSucc)

    //println("inside find_predecessor : actor: "+currActorNodeIndex+" : predecessor "+tempActorPred.asInstanceOf[].predecessor.toString)

    while(((tempCurrNode > tempSucc && (fingerNodeValue <= tempCurrNode && fingerNodeValue > tempSucc)) ||
      (tempCurrNode < tempSucc && (fingerNodeValue <= tempCurrNode || fingerNodeValue > tempSucc)))
      &&  (tempCurrNode != tempSucc )) {
      if (tempCurrNode == currActorNodeIndex)
      {
        println("tempcurrnode = curractornode")
        tempCurrNode_dash = closest_preceding_finger(fingerNodeValue, tempCurrNode)
      }
      else {

        println("tempcurrnode != curractornode. tempCurrNode: "+tempCurrNode)
        val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode.toString)
        val futureNode = tempActor ? GetClosesNodes(fingerNodeValue, tempCurrNode)

        tempCurrNode_dash= Await.result(futureNode, timeout.duration).asInstanceOf[Int]

        println("after await in find_predecessor: "+tempCurrNode)
      }
      if(tempCurrNode_dash != tempCurrNode){
        val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode_dash.toString)
        val futureSucc = tempActor ? GetSuccessor(tempCurrNode_dash)
        tempSucc = Await.result(futureSucc, timeout.duration).asInstanceOf[Int]
        tempCurrNode = tempCurrNode_dash
      }


    }

    println("Returning from find predecessor with value: "+tempCurrNode)
    return tempCurrNode
  }

  def closest_preceding_finger(fingerNodeVale : Int, currActorNodeIndex : Int):Int={
    println("inside closest preceding finger : ")
    var count:Int = m
    while(count > 0 )
    {
      println("Inside while with count :"+count)

      if  ((currActorNodeIndex > fingerNodeVale  && (fingerTable(count-1)(1) > currActorNodeIndex ||
        fingerTable(count-1)(1) < fingerNodeVale))
        || (currActorNodeIndex < fingerNodeVale && fingerTable(count-1)(1) > currActorNodeIndex && fingerTable(count-1)(1) < fingerNodeVale)
        || (currActorNodeIndex == fingerNodeVale && currActorNodeIndex != fingerTable(count-1)(1))  )


        return fingerTable(count-1)(1);

      count = count - 1
    }
    println("else returning current node actor index : "+currActorNodeIndex)
    return currActorNodeIndex
  }

  def Stabilize(currActorNodeIndex:Int) {
    var result: Int = -1
    val tempSucc = successor //current successor
    if (currActorNodeIndex == tempSucc) {
      result = this.predecessor
      println("currActorNodeIndex == tempSucc " + tempSucc + " and predecessor = " + result)
    }
    else {
      val node = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempSucc.toString)

      val futurePred = node ? GetPredecessor(tempSucc)
      result = Await.result(futurePred, timeout.duration).asInstanceOf[Int]

      println("after await : stabilize " + tempSucc + " and predecessor = " + result)
    }
    if (result > -1 &&
      ((currActorNodeIndex > successor && (result > currActorNodeIndex || result < successor)) ||
        (currActorNodeIndex < successor && result > currActorNodeIndex && result < successor)
        || currActorNodeIndex == successor && result != currActorNodeIndex)) {
      successor = result;
    }

    if (currActorNodeIndex == successor){
      self ! NotifyNode(currActorNodeIndex, successor)
    }
    else{
      val node = context.actorSelection("akka://ChordProtocolHW4/user/node_" + successor.toString)
      node ! NotifyNode(currActorNodeIndex, successor)
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
    var totalNodes = 8
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