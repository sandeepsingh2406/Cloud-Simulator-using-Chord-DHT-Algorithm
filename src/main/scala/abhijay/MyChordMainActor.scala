package abhijay

import java.security.MessageDigest

import akka.actor.{Actor, ActorSystem, Props}

import scala.util.Random

/**
  * Created by avp on 11/20/2016.
  */

case class CreateRing(nodeindex : Int)

class MyChordMainActor(TotalNodes: Int ,MinMsgs: Int, MaxMsgs: Int, listFileItems : String,SimulationDuration: Int,
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
        //println("Inside for loop - instantiating actors for each computer in cloud with node: "+i)
        val workerNodes = ChordActorSys.actorOf(Props(new MyCloudNodeActor(TotalNodes, activenodes, MinMsgs, MaxMsgs, listFileItems, SimulationDuration, SimluationMark, i, self)), name = "node_" + i.toString)
        workerNodes ! "initiateEachNode"
      }
      self ! ActivateNodeInRing(0)

      //self ! CreateRing(0)
    }

      /* Create a ring for the first time*/

    case CreateRing(nodeIndex : Int) =>{

    }

    /* assign the nodes in ring*/
    case ActivateNodeInRing(nodeIndex : Int)=> {
      println("Activate the the node in ring with index: "+nodeIndex)
      println("Node at index: "+nodeIndex+" hashed value: "+NodeArrayActors(nodeIndex)(0).toString)

      /* use the akka actor selection to call each actor as initiated for total nodes */
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
      //eachnodeactor.nod
      NodeArrayActors(nodeIndex)(1) = "1" //assuming this node will be successfully joined - making this active
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
    var tempHashValues = new Array[String](TotalNodes);

    /* udpate the array to store the hashed value for a random generated string for only the active nodes */
    for (count <- 0 to TotalNodes - 1) {
      randomStr = Random.alphanumeric.take(40).mkString
      //println("Random String: "+randomStr)
      hashValues = md5(randomStr)
      //println("Hash value from MD5 digest: "+hashValues)
      val forHash = Math.pow(2.toDouble,m.toDouble-1).toInt

      hashValues = hashValues.substring(0,forHash)

//      println("hashvalue after substring : "+hashValues)

      /**  hashed value for each active node : **/
/*
      NodeArrayActors(count)(0) = hashValues
      NodeArrayActors(count)(1) = "0"
*/
      tempHashValues(count) = hashValues;

    }
    /*scala.util.Sorting.quickSort(NodeArrayActors)*/
    scala.util.Sorting.quickSort(tempHashValues)
    /* Print Sort the calculated hashes */
    for (count <- 0 to TotalNodes - 1) {
      NodeArrayActors(count)(0) = tempHashValues(count)
      NodeArrayActors(count)(1) = "0"
//      println("Sorted Hashed Node at Index: "+count+" key: "+NodeArrayActors(count)(0))
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

