package abhijay

import java.security.MessageDigest

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import scala.util.Random
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.Await
/**
  * Created by avp on 11/20/2016.
  */

case class GetPredecessor(tempSucc:Int)

case class GetSuccessor(tempNode: Int)

case class NotifyNode(notifyThisNode : Int, currentCallingNode : Int)

case class GetClosesNodes(fingerNodeValue : Int,tempCurrNode : Int)

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class MyCloudNodeActor(TotalNodes:Int, ActiveNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
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
    case "initiateEachNode" => {
      //println("Initiate Node")
    }

    case JoinNode(nodeArrayActors:Array[String], nodeIndex:Int) => {
      isActiveNode = 1
      successor = nodeIndex
      println("In Join node for each actor reperesenting the active node in ring - now joining ring")
      for(i<-0 to m-1)
      {
//        println("Value inserted at index: "+i+" is: "+((Index+math.pow(2,i).toInt)%TotalNodes))
        /* calculate the node that the current node actor*/
        fingerTable(i)(0) = (Index+math.pow(2,i).toInt)%(TotalNodes)
        fingerTable(i)(1) = nodeIndex
      }

      println("Get Successor for node: "+nodeIndex)

      createAndUpdateFingers(nodeIndex);
//      fillFingerTable(nodeIndex)
      Stabilize(nodeIndex)

      // fix_fingers(nodeIndex)

      //Checks if all the active nodes are added to the network. If not adds the remaining nodes,
      // else if all the active nodes are added then starts the process for message passing.
      if(nodeIndex <= (ActiveNodes-1)) {
        println("When all active nodes are not yet a part of ring,call the master node again to add the active node to the ring")
        sender ! ActivateNodeInRing(nodeIndex + 1)
      }
      else{
        println("Now starting web service which helps to interact with cloud system")
/*
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

    case GetPredecessor(tempSucc:Int) =>{
      println("Get Predecessor for node: "+tempSucc)
      sender ! predecessor
    }

    case GetSuccessor(tempNode: Int) =>{
      println("Get Successor for node: "+tempNode)
      sender ! successor
    }

    case NotifyNode(notifyThisNode : Int, currentCallingNode : Int) => {
      println("In notify node for notifying: "+notifyThisNode+" with calling node index: "+currentCallingNode)
      var tempResult :Int = -1;

      if(predecessor > currentCallingNode && ((notifyThisNode > predecessor) || (notifyThisNode < currentCallingNode))){
        tempResult = 1
      }
      else if(predecessor < currentCallingNode && ((notifyThisNode > predecessor) && (notifyThisNode < currentCallingNode))){
        tempResult = 1
      }
      else if(predecessor == currentCallingNode && notifyThisNode != predecessor){
        tempResult = 1
      }

      if(tempResult == 1 || predecessor == -1){
        predecessor = notifyThisNode
      }
      println("New predecessor for node: "+currentCallingNode+" value is: "+predecessor)
    }

    case GetClosesNodes(fingerNodeValue : Int,tempCurrNode : Int) => {

      val tempNode = closest_preceding_finger(fingerNodeValue,tempCurrNode)
      sender ! tempNode

    }

  }

  /* fillFingerTable: fill the respective successor values in a finger table*/
  def fillFingerTable(currActorNodeIndex : Int): Unit ={

    println("Inside get successor for node: "+currActorNodeIndex)
    for(i <- 0 until m){
      val tempFingerNode = fingerTable(i)(0)
      println("fillFingerTable() actorIndex: "+currActorNodeIndex+" with finger table node: "+i+" and value :"+tempFingerNode)
      val getSucc = find_successor(tempFingerNode,currActorNodeIndex)
      println("New successor received as: "+getSucc)
      fingerTable(i)(1) = getSucc
    }
  }

  def find_successor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int = {
    println("Inside Find successor. Call find_predecessor for node: "+currActorNodeIndex+" with finger table start value: "+fingerNodeValue)
    val newSucc = find_predecessor(fingerNodeValue,currActorNodeIndex)

    val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+newSucc.toString)
    var result : Int = 0
    try {
      val futureSucc = tempActor ? GetSuccessor(newSucc)

      result = Await.result(futureSucc, timeout.duration).asInstanceOf[Int].toInt
    }
    catch {
      case ex: Exception => {
        println("Exception in find successor")
      }
    }

    return result //need to send the newSucc.successor - call the finger table of this new succ node
  }

  def find_predecessor(fingerNodeValue : Int ,currActorNodeIndex: Int): Int ={

    println("Inside find predecssor")
    var tempCurrNode = currActorNodeIndex
    val tempSucc = successor
    println("find predecssor : Finger table : Successor for node: "+currActorNodeIndex+" value: of finger[1].node= "+tempSucc)
    println("find predecssor : Comparing: fingerNodeValue: "+fingerNodeValue+" in range of tempCurrNode= "+tempCurrNode+" and <= tempSucc= "+tempSucc)

    var tempResult = -1

    if(tempCurrNode > tempSucc && ((fingerNodeValue > tempCurrNode) || (fingerNodeValue <= tempSucc))){
      tempResult = 1
    }
    else if(tempCurrNode < tempSucc && ((fingerNodeValue > tempCurrNode) && (fingerNodeValue <= tempSucc))){
      tempResult = 1
    }
    else if(tempCurrNode == tempSucc && fingerNodeValue == tempCurrNode){
      tempResult = 1
    }

    while(tempResult == 1){
      println("find predecssor : inside while. Call closes preceding finger")
      val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+tempCurrNode.toString)

      try
      {

        val futureNode = tempActor ? GetClosesNodes(fingerNodeValue, tempCurrNode)

        tempCurrNode = Await.result(futureNode, timeout.duration).asInstanceOf[Int]


        tempResult = -1

        if(tempCurrNode > tempSucc && ((fingerNodeValue > tempCurrNode) || (fingerNodeValue <= tempSucc))){
          tempResult = 1
        }
        else if(tempCurrNode < tempSucc && ((fingerNodeValue > tempCurrNode) && (fingerNodeValue <= tempSucc))){
          tempResult = 1
        }
        else if(tempCurrNode == tempSucc && fingerNodeValue == tempCurrNode){
          tempResult = 1
        }
      }
      catch {
        case ex: Exception => {
          println("exception in get get closes nodes")
        }
      }

      //tempCurrNode = closest_preceding_finger(fingerNodeValue,tempCurrNode)
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
      var tempRes = -1

      if(currActorNodeIndex > fingerNodeVale && ((fingerTable(count)(1) > currActorNodeIndex) || (fingerTable(count)(1) < fingerNodeVale))){
        tempRes = 1
      }
      else if(currActorNodeIndex > fingerNodeVale && ((fingerTable(count)(1) > currActorNodeIndex) && (fingerTable(count)(1) < fingerNodeVale))){
        tempRes = 1
      }
      else if(currActorNodeIndex == fingerNodeVale && fingerTable(count)(1) == currActorNodeIndex){
        tempRes = 1
      }

      if(tempRes == 1){
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
    val tempSucc = fingerTable(0)(1)
    val node = context.actorSelection("akka://ChordProtocolHW4/user/node_"+tempSucc.toString)

    val insidetimeout = Timeout(20 seconds)

    val futurePred = node ? GetPredecessor(tempSucc)
    val result = Await.result(futurePred, insidetimeout.duration).asInstanceOf[Int]

    if(result != -1 )
    {
      if(currActorNodeIndex > tempSucc && ((result >currActorNodeIndex) || (result < tempSucc))){
        fingerTable(0)(1) = result
      }
      else if(currActorNodeIndex < tempSucc && ((result > currActorNodeIndex) && (result < tempSucc))){
        fingerTable(0)(1) = result
      }
      else if(currActorNodeIndex == tempSucc && result != currActorNodeIndex){
        fingerTable(0)(1) = result
      }

    }
    node ! NotifyNode(currActorNodeIndex,tempSucc)

    /*        System.out.println("this.next_finger " + this.next_finger);
        while (true) {

            if (this.next_finger > 3) {
                this.next_finger = 1;
            }

            int id =(this.id + (int) Math.pow(2, this.next_finger - 1))%(int)Math.pow(2, 3);

            this.finger_table[this.next_finger - 1][0] = id;
            this.finger_table[this.next_finger - 1][1] = this.Locate_Successor(id);

            this.next_finger++;

            if (this.next_finger > 3)
                break;

        }
*/
    }

  // create and update finger table for node nodeIndex
  def createAndUpdateFingers(nodeIndex : Int): Unit ={
    for(i <- 0 until m){
      fingerTable(i)(0) = ((nodeIndex + scala.math.pow(2, i-1)).toInt)% ((scala.math.pow(2, m-1)).toInt)
    }
    fillFingerTable(nodeIndex)
  }


}

