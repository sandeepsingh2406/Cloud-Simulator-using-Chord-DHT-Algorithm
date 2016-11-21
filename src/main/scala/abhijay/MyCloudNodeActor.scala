package abhijay

import akka.actor.{Actor, ActorRef}
import akka.util.Timeout
import scala.concurrent.duration._

/**
  * Created by avp on 11/20/2016.
  */

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class MyCloudNodeActor(TotalNodes:Int, ActiveNodes: Int, MinMsgs: Int, MaxMsgs: Int, listFileItems : String, SimulationDuration: Int,
                       SimluationMark : Int, Index: Int, ChordActorSys:ActorRef) extends Actor {

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
        eachnodeactor ! PrintFingerTable(i)
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
