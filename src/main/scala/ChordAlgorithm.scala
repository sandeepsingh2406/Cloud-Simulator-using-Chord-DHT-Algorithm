import java.security.MessageDigest

import akka.actor.{Actor, ActorRef, ActorSystem, Props, _}
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

case class GetNodeHashedActors(nodeIndex : Int)

case class GetItemDetail(itemString : String,nodeIndex : Int)

case class PutItemDetail(itemName : String ,itemDetail : String,nodeIndex : Int)

case class GetHashedValue(nodeIndex : Int)

case class FindSuccessor(fingerNodeValue : String ,currActorNodeIndex: Int, requestOrigin : String)

case class LocateSuccessor(nodeIndex : Int)

case class JoinNode(newNode: Int, existingNode: Int)

case class ActivateNodeInRing(nodeIndex:Int)

case class ActivateOtherNode(existingNode : Int)

case class StabilizeRing(nodeIndex : Int)

case class CreateRing(nodeArrayActors:Array[String],nodeIndex:Int)

case class PrintFingerTable(nodeIndex : Int)

case class UpdateSuccessor(nextNodeActorIndex : Int)

case class GetPredecessor(tempSucc:Int)

case class GetSuccessor(tempNode: Int)

case class NotifyNode(notifyThisNode : Int, currentCallingNode : Int)

case class GetClosesNodes(fingerNodeValue : String ,tempCurrNode : Int, requestOrigin : String)


class ChordMainActor(TotalNodes: Int ,SimulationDuration: Int, SimluationMark : Int,ChordActorSys: ActorSystem) extends Actor {

  var activenodes: Int = 0
  var NodeArrayActors  = Array.ofDim[String](TotalNodes)
  var m:Int = 0
  implicit val timeout = Timeout(20 seconds)

  def receive = {

    case "startProcess" => {
      println("In Start Process")
      println("Master Node has been Initiated")
      println("Total nodes in the cloud: " + TotalNodes)

      /* total number of nodes present in the system : 2 ^ m*/
      activenodes = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
      m = activenodes

      println("Finger Table rows : "+m)

      println("Node of array actors length: maintaining the hashes for each computer/actor length:" + NodeArrayActors.length)
      println("Total number of active nodes in the cloud: " + activenodes)

      /* on the basis of the total nodes - each node must be created - that is intiated as an actor*/

      InitializeNodeHashTable

      for (i <- 0 until TotalNodes) {
        println("Inside for loop - instantiating actors for each computer in cloud with node: "+i)
        val workerNodes = ChordActorSys.actorOf(Props(new CloudNodeActor(NodeArrayActors(i),TotalNodes, activenodes, SimulationDuration, SimluationMark, i, self)), name = "node_" + i.toString)
        val futureWorker = workerNodes ? "intiateEachNode"
        println(Await.result(futureWorker, timeout.duration).asInstanceOf[String])
      }

      sender ! "done"

    }

    /* assign the nodes in ring*/
    case ActivateNodeInRing(nodeIndex : Int)=> {
      val orginalSender = sender
      println("Activate the the node in ring with index: "+nodeIndex)
      println("Node at index: "+nodeIndex+" hashed value: "+NodeArrayActors(nodeIndex).toString)
      chordMainMethod.ActorJoined+=nodeIndex

      /* use the akka actor selection to call each actor as intiated for totoal nodes */
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)

      val futureNewNode = eachnodeactor ? CreateRing(NodeArrayActors,nodeIndex)
      println(Await.result(futureNewNode, timeout.duration).asInstanceOf[String])

      println("After create ring wth node: "+nodeIndex+ " finger table values: ")
      FetchFingerTable

      orginalSender ! "done"
    }

    case GetNodeHashedActors(nodeIndex : Int) => {
      val orginalSender = sender
      orginalSender ! NodeArrayActors(nodeIndex)
    }

    case ActivateOtherNode(newNode : Int) => {
      val orignalSender = sender
        val random = new Random
        val newRandom = chordMainMethod.ActorJoined(random.nextInt(chordMainMethod.ActorJoined.length))
        println("New random: "+newRandom)

        var eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+newNode.toString)

        var futureNode = eachnodeactor ? JoinNode(newNode,newRandom)

        val result = Await.result(futureNode, timeout.duration).asInstanceOf[String]

        println("Returned: "+result)
        chordMainMethod.ActorJoined+=newNode

        println("Existing nodes: "+chordMainMethod.ActorJoined)


        for(counter <- 0 until chordMainMethod.ActorJoined.length)
        {
          for(insidecounter <- 0 until chordMainMethod.ActorJoined.length)
          {
            var futureStabilize = eachnodeactor ? StabilizeRing(newNode)
            println(Await.result(futureStabilize, timeout.duration).asInstanceOf[String])

            var neweachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+chordMainMethod.ActorJoined(insidecounter).toString)

            futureNode = neweachnodeactor ? StabilizeRing(chordMainMethod.ActorJoined(insidecounter))

            println(Await.result(futureNode, timeout.duration).asInstanceOf[String])


            futureStabilize = eachnodeactor ? LocateSuccessor(newNode)
            println(Await.result(futureStabilize, timeout.duration).asInstanceOf[String])

            futureNode = neweachnodeactor ? LocateSuccessor(chordMainMethod.ActorJoined(insidecounter))

            println(Await.result(futureNode, timeout.duration).asInstanceOf[String])
          }
          println("After adding: "+newNode )
          FetchFingerTable

          println("Nodes added in system: "+chordMainMethod.ActorJoined)

        }


      orignalSender ! "done"

    }

  }

  def FetchFingerTable: Unit = {
    for(i <- 0 until chordMainMethod.ActorJoined.length){
      println("Printing for node: "+chordMainMethod.ActorJoined(i))
      val eachnodeactor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+chordMainMethod.ActorJoined(i).toString)
      val future = eachnodeactor ? PrintFingerTable(chordMainMethod.ActorJoined(i))
      println(Await.result(future, timeout.duration).asInstanceOf[String])
    }
  }

  def InitializeNodeHashTable: Unit = {

    var count:Int = 0
    var nodeString:String = ""
    var hashValue:String = ""

    /* udpate the array to store the hashed value for a random generated string for only the active nodes */
    while (count < TotalNodes) {

      /* used a  random string for now- generally this will be the computers IP address */
      nodeString = Random.alphanumeric.take(25).mkString

      hashValue = chordMainMethod.getHash(nodeString,m)

      //hashValue = getHash(nodeString,TotalNodes)
      println("Hash value for: "+nodeString+" is: "+hashValue)

      /**  hashed value for each active node : **/
      if(NodeArrayActors.contains(hashValue)){
        //skip this , count not incremented
      }
      else{
        NodeArrayActors(count) = hashValue
        count = count + 1
      }
    }
    scala.util.Sorting.quickSort(NodeArrayActors)
    /* Print Sort the calculated hashes */
    for (count <- 0 until TotalNodes) {
      chordMainMethod.SortedHashedActor += NodeArrayActors(count)
      println("Sorted Hashed Node at Index: "+count+" key: "+chordMainMethod.SortedHashedActor(count))
    }
    /* activate the first node in the ring */

  }

}

/* Actor class for each of the nodes present in the cloud. These are the total number of nodes int the cloud*/
class CloudNodeActor(HashedValue: String,TotalNodes:Int, ActiveNodes: Int ,SimulationDuration: Int,
                     SimluationMark : Int,Index: Int,ChordActorSys:ActorRef) extends Actor {

  var nodeHopsCounter:Int=0
  val m: Int = ((Math.log10(TotalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
  val fingerTable = Array.ofDim[Int](m,2)
  implicit val timeout = Timeout(20 seconds)
  var predecessor :Int = -1
  var successor : Int = -1
  var next_finger: Int = 1
  var isActiveNode : Int = -1

  var listOfItems = scala.collection.mutable.HashMap[String, String]()
  //var listOfItems : ListBuffer[String] = new ListBuffer[String]

  def receive = {
    case "intiateEachNode" => {
      val orignalSender = sender
      println("Initiate Node: "+Index+" with Hashed Value: "+HashedValue)
      orignalSender ! "done"
    }

    case JoinNode(newNode:  Int,existingNode : Int) => {
      val orignalSender = sender
      isActiveNode = 1
      successor = newNode

      println("In JoinNode for node: "+newNode+" with previous node: "+existingNode)

      println("Initialize finger table : Only Column 1:")
      for(i<-0 until m)
      {
        println("Value inserted at index: "+i+" is: "+((Index+math.pow(2,i).toInt)%TotalNodes))
        /* calculate the node that the current node actor*/
        fingerTable(i)(0) = (Index+math.pow(2,i).toInt)%TotalNodes
      }

      val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_"+existingNode.toString)
      val futureNodeSucc = tempActor ? FindSuccessor(newNode.toString,existingNode,"self")

      this.successor = Await.result(futureNodeSucc, timeout.duration).asInstanceOf[Int]

      println("After FindSuccessor case: new successor for new node: "+newNode+" value is: "+this.successor)
      orignalSender ! "JoinNode Done for: "+newNode
    }

    case CreateRing(nodeArrayActors:Array[String], nodeIndex:Int) => {
      val orignalSender = sender
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

      orignalSender ! "Create Ring done with node: "+nodeIndex
    }

    case GetItemDetail(itemString : String,nodeIndex : Int) => {
      println("Get item string : "+itemString+ " from node: "+ nodeIndex)
      val orignalSender = sender
      val itemExists = this.listOfItems.get(itemString)
      if(itemExists.size == 0) {
        println("Item not found: "+itemString+" at node: "+nodeIndex)
        orignalSender ! "not found"
      }
      else{
        println("Item found: "+itemString+" at node: "+nodeIndex+" with value: "+itemExists.toString)
        orignalSender ! itemExists.toString
      }
    }

    case PutItemDetail(itemName : String ,itemDetail : String,nodeIndex : Int) =>{
      println("Put item name : "+itemName+ " with details: "+itemDetail+" at node: "+ nodeIndex)
      val orignalSender = sender

      val tempItemDetail = itemName + " , " + itemDetail
      this.listOfItems += (itemName -> tempItemDetail)
      println("New items at node: "+nodeIndex+" are: "+this.listOfItems.values)
      orignalSender ! "done"
    }

    case PrintFingerTable(nodeIndex:Int) => {
      val orignalSender = sender
      println("Node: "+nodeIndex+" Successor: "+this.successor+" and Predecesso: "+this.predecessor)
      for(i<-0 to (fingerTable.length-1)){
        println("Node: "+nodeIndex+" Finger table values at index: "+i+" is: "+fingerTable(i)(0)+" successor: "+fingerTable(i)(1))
      }
      orignalSender ! "done for: "+nodeIndex
    }

    case GetPredecessor(tempSucc:Int) =>{
      val orignalSender = sender
      println("Get Predecessor for node: "+tempSucc+" value: "+this.predecessor)
      orignalSender ! this.predecessor
    }

    case StabilizeRing(nodeIndex : Int) => {
      val orignalSender = sender
      Stabilize(nodeIndex)
      orignalSender ! "Stabilize Done for "+nodeIndex
    }

    case GetSuccessor(tempNode: Int) =>{
      val orignalSender = sender
      println("Get Successor for node: "+tempNode+" value: "+this.successor)
      orignalSender ! this.successor

    }
    case GetHashedValue(nodeIndex : Int) =>{
      val orignalSender = sender
      println("Get Hashed Value for node: "+nodeIndex+" value: "+this.HashedValue)
      orignalSender ! this.HashedValue
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

    case GetClosesNodes(fingerNodeValue : String ,tempCurrNode : Int, requestOrigin : String) => {
      val orignalSender = sender
      val tempNode = closest_preceding_finger(fingerNodeValue,tempCurrNode, requestOrigin)
      orignalSender ! tempNode
    }

    case FindSuccessor(fingerNodeValue : String ,currActorNodeIndex: Int, requestOrigin : String) =>{
      val orignalSender = sender
      val tempSuccVal = find_successor(fingerNodeValue,currActorNodeIndex,requestOrigin)
      orignalSender ! tempSuccVal
    }

    case LocateSuccessor(nodeIndex : Int) => {
      val orignalSender = sender
      val tempSuccVal = locate_successor(nodeIndex)
      orignalSender ! "Fixed Finger for " +nodeIndex
    }

  }

  def locate_successor(currActorNodeIndex : Int): Unit ={

    println("Inside locate successor for node: "+currActorNodeIndex)
    for(i <- 0 until m){
      val tempFingerNode = fingerTable(i)(0)
      println("Call find_successor for  node: "+currActorNodeIndex+" with finger table node: "+i+" and value :"+tempFingerNode)
      val getSucc = find_successor(tempFingerNode.toString,currActorNodeIndex, "self")
      println("New successor received as: "+getSucc)
      fingerTable(i)(1) = getSucc
    }
  }

  def find_successor(fingerNodeValue : String ,currActorNodeIndex: Int, requestOrigin : String): Int = {
    println("Inside Find successor. Call find_predecessor for node: "+currActorNodeIndex+" with finger table start value: "+fingerNodeValue)

    var fetchRes : Int = -1

    val newSucc = find_predecessor(fingerNodeValue,currActorNodeIndex,requestOrigin)

      if (currActorNodeIndex == newSucc) {
        println("Current node same as prev")
        fetchRes = this.successor
      }
      else {
        val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + newSucc.toString)


        val futureSucc = tempActor ? GetSuccessor(newSucc)

        fetchRes = Await.result(futureSucc, timeout.duration).asInstanceOf[Int]
      }


     println("find_succ : after on success: "+fetchRes)

    return fetchRes
  }

  def find_predecessor(fingerNodeValue : String ,currActorNodeIndex: Int, requestOrigin : String): Int ={

    println("Inside find predecssor")
    var tempCurrNode_dash : Int = 0
    var tempCurrNode = currActorNodeIndex
    var tempSucc = this.successor
    println("find predecssor : Finger table : Successor for node: "+currActorNodeIndex+" value: of finger[1].node= "+tempSucc)

    if(requestOrigin.toLowerCase().equals("self"))
    {
      while (((tempCurrNode > tempSucc && (fingerNodeValue.toInt <= tempCurrNode && fingerNodeValue.toInt > tempSucc)) ||
        (tempCurrNode < tempSucc && (fingerNodeValue.toInt <= tempCurrNode || fingerNodeValue.toInt > tempSucc)))
        && (tempCurrNode != tempSucc))
      {
        if (tempCurrNode == currActorNodeIndex)
        {
          println("tempcurrnode = curractornode")
          tempCurrNode_dash = closest_preceding_finger(fingerNodeValue, tempCurrNode,requestOrigin)
        }
        else
        {
          println("tempcurrnode != curractornode. tempCurrNode: " + tempCurrNode)
          val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode.toString)
          val futureNode = tempActor ? GetClosesNodes(fingerNodeValue, tempCurrNode,requestOrigin)

          tempCurrNode_dash = Await.result(futureNode, timeout.duration).asInstanceOf[Int]

          println("after await in find_predecessor: " + tempCurrNode)
        }
        if (tempCurrNode_dash != tempCurrNode)
        {
          val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode_dash.toString)
          val futureSucc = tempActor ? GetSuccessor(tempCurrNode_dash)
          tempSucc = Await.result(futureSucc, timeout.duration).asInstanceOf[Int]
          tempCurrNode = tempCurrNode_dash
        }
      }
    }
    else if(requestOrigin.toLowerCase().equals("user"))
    {
      var tempCurrNode_Hash : String = ""
      var tempSucc_Hash : String = ""

      if (tempCurrNode != currActorNodeIndex){
        val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode.toString)
        val futureNode = tempActor ? GetHashedValue(tempCurrNode)
        tempCurrNode_Hash = Await.result(futureNode, timeout.duration).asInstanceOf[String]
      }
      else{
        tempCurrNode_Hash = this.HashedValue
      }

      if (tempSucc != currActorNodeIndex){
        val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempSucc.toString)
        val futureNode = tempActor ? GetHashedValue(tempSucc)
        tempSucc_Hash = Await.result(futureNode, timeout.duration).asInstanceOf[String]
      }
      else{
        tempSucc_Hash = this.HashedValue
      }

      while (((tempCurrNode_Hash > tempSucc_Hash && (fingerNodeValue <= tempCurrNode_Hash && fingerNodeValue > tempSucc_Hash)) ||
        (tempCurrNode_Hash < tempSucc_Hash && (fingerNodeValue <= tempCurrNode_Hash || fingerNodeValue > tempSucc_Hash)))
        && (tempCurrNode_Hash != tempSucc_Hash))
      {
        if (tempCurrNode == currActorNodeIndex)
        {
          println("tempcurrnode = curractornode")
          tempCurrNode_dash = closest_preceding_finger(fingerNodeValue, tempCurrNode,requestOrigin)
        }
        else
        {
          println("tempcurrnode != curractornode. tempCurrNode: " + tempCurrNode)
          val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode.toString)
          val futureNode = tempActor ? GetClosesNodes(fingerNodeValue, tempCurrNode,requestOrigin)

          tempCurrNode_dash = Await.result(futureNode, timeout.duration).asInstanceOf[Int]

          println("after await in find_predecessor: " + tempCurrNode)
        }

        if (tempCurrNode_dash != tempCurrNode)
        {
          val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode_dash.toString)
          val futureSucc = tempActor ? GetSuccessor(tempCurrNode_dash)
          tempSucc = Await.result(futureSucc, timeout.duration).asInstanceOf[Int]
          tempCurrNode = tempCurrNode_dash

          if (tempCurrNode != currActorNodeIndex){
            val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempCurrNode.toString)
            val futureNode = tempActor ? GetHashedValue(tempCurrNode)
            tempCurrNode_Hash = Await.result(futureNode, timeout.duration).asInstanceOf[String]
          }
          else{
            tempCurrNode_Hash = this.HashedValue
          }

          if (tempSucc != currActorNodeIndex){
            val tempActor = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempSucc.toString)
            val futureNode = tempActor ? GetHashedValue(tempSucc)
            tempSucc_Hash = Await.result(futureNode, timeout.duration).asInstanceOf[String]
          }
          else{
            tempSucc_Hash = this.HashedValue
          }
        }
      }
    }

    println("Returning from find predecessor with value: "+tempCurrNode)
    return tempCurrNode
  }


  def closest_preceding_finger(fingerNodeVale : String, currActorNodeIndex : Int, requestOrigin : String):Int={
    println("inside closest preceding finger : ")
    var count:Int = m

    if(requestOrigin.toLowerCase().equals("self")) {
      while (count > 0) {
        if ((currActorNodeIndex > fingerNodeVale.toInt && (fingerTable(count - 1)(1) > currActorNodeIndex ||
          fingerTable(count - 1)(1) < fingerNodeVale.toInt))
          || (currActorNodeIndex < fingerNodeVale.toInt && fingerTable(count - 1)(1) > currActorNodeIndex && fingerTable(count - 1)(1) < fingerNodeVale.toInt)
          || (currActorNodeIndex == fingerNodeVale.toInt && currActorNodeIndex != fingerTable(count - 1)(1)))
        {
          println("Returning from current node: "+currActorNodeIndex+"with row count: "+count+" value: " + fingerTable(count - 1)(1))
          return fingerTable(count - 1)(1);
        }

        count = count - 1
      }
    }
    else if(requestOrigin.toLowerCase().equals("user"))
    {
      val currActorNodeIndex_hash = this.HashedValue

      var fingerTableValue_hash :String = ""

      val tempIndex = fingerTable(count - 1)(1)
      if(tempIndex != currActorNodeIndex)
      {
        val node = context.actorSelection("akka://ChordProtocolHW4/user/node_" + tempIndex.toString)

        val futureHash = node ? GetHashedValue(tempIndex)
        fingerTableValue_hash = Await.result(futureHash, timeout.duration).asInstanceOf[String]
      }
      else{
        fingerTableValue_hash = this.HashedValue
      }

      while (count > 0)
      {
        if ((currActorNodeIndex_hash > fingerNodeVale && (fingerTableValue_hash > currActorNodeIndex_hash ||
          fingerTableValue_hash < fingerNodeVale))
          || (currActorNodeIndex_hash < fingerNodeVale && fingerTableValue_hash > currActorNodeIndex_hash && fingerTableValue_hash < fingerNodeVale)
          || (currActorNodeIndex_hash == fingerNodeVale && currActorNodeIndex_hash != fingerTableValue_hash))

          return fingerTable(count - 1)(1);

        count = count - 1
      }
    }
    println("else returning current node actor index : "+currActorNodeIndex)
    return currActorNodeIndex
  }

  def Stabilize(currActorNodeIndex:Int) {
    var result: Int = -1
    val tempSucc = this.successor //current successor
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

  implicit val timeout = Timeout(20 seconds)

  var SortedHashedActor : ListBuffer[String] = new ListBuffer[String]()

  var ActorJoined : ListBuffer[Int] = new ListBuffer[Int]()

  var totalNodes : Int = 0

  var simulationDuration : Int = 0

  var simulationMark : Int = 0

  val system = ActorSystem("ChordProtocolHW4")

  var nodeSpace : Int = -1

  def getHash(key:String, m : Int): String = {

    val sha_instance = MessageDigest.getInstance("SHA-1")
    var sha_value:String =sha_instance.digest(key.getBytes).foldLeft("")((s:String, b: Byte) => s + Character.forDigit((b & 0xf0) >> 4, 16) +Character.forDigit(b & 0x0f, 16))
    var generated_hash:String =sha_value.substring(0,m-1)
    return generated_hash
  }

  def main(args: Array[String])
  {
    println("Input Arguements: No of User, No of nodes, min and max msgs (by each user), simulation duraion , time mark for simulation, items list,  ")

    println("Enter total nodes in system: ")
    totalNodes = scala.io.StdIn.readInt()

    println("Enter system simuation duration: ")
    simulationDuration = scala.io.StdIn.readInt()

    println("Enter system simuation mark: ")
    simulationMark = scala.io.StdIn.readInt()

    nodeSpace = ((Math.log10(totalNodes.toDouble)) / (Math.log10(2.toDouble))).ceil.toInt
    val Master = system.actorOf(Props(new ChordMainActor(totalNodes,simulationDuration,simulationMark,system)), name = "MainActor")
    val futureMaster = Master ? "startProcess"
    println(Await.result(futureMaster, timeout.duration).asInstanceOf[String]+" instantiating chord simulator")

    val inst: Service = new Service()
    inst.method(new Array[String](5))



  }

  def CreateRingWithNode( nodeIndex : Int): String = {

    println("Create ring with node: "+nodeIndex)
    val actorRef=system.actorSelection("akka://ChordProtocolHW4/user/"+"MainActor")
    //println(actorRef.pathString)

    val future = actorRef ? ActivateNodeInRing(nodeIndex)
    println(Await.result(future, timeout.duration).asInstanceOf[String]+nodeIndex+" Node is activated")

    return "done"
  }

  def InsertNodeInRing( nodeIndex : Int): String = {

    println("Insert node in ring with index: "+nodeIndex)
    val actorRef=system.actorSelection("akka://ChordProtocolHW4/user/"+"MainActor")
    //println(actorRef.pathString)

    val future = actorRef ? ActivateOtherNode(nodeIndex)
    println(Await.result(future, timeout.duration).asInstanceOf[String]+nodeIndex+" Node is activated")

    return "done"
  }

  def LookupItem(itemString : String) : String = {

    val random = new Random
    val newRandom = chordMainMethod.ActorJoined(random.nextInt(chordMainMethod.ActorJoined.length))

    println("Lookup item: "+itemString+ " first random node: "+newRandom)

    val itemString_hash = getHash(itemString.toLowerCase(),nodeSpace)
    println("item hash : "+itemString_hash)

    val actorRef=system.actorSelection("akka://ChordProtocolHW4/user/node_"+newRandom.toString)
    //println(actorRef.pathString)

    val future = actorRef ? FindSuccessor(itemString_hash,newRandom,"user")

    val succRes = Await.result(future, timeout.duration).asInstanceOf[Int]

   // LookupListItem(succRes,itemString.toLowerCase())
    println("Look up item : response from find_successor : "+succRes)

    return succRes.toString
  }

  def LookupListItem(nodeIndex:Int, itemString : String) : String = {
    println("Lookup each item: "+itemString+" at node: "+nodeIndex)

    val actorRef=system.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
    //println(actorRef.pathString)
    val future = actorRef ? GetItemDetail(itemString.toLowerCase(),nodeIndex)

    val itemReceived = Await.result(future, timeout.duration).asInstanceOf[String]

    return itemReceived
  }

  def InsertItem(nodeIndex: Int, itemName : String ,itemDetail : String) : String ={

    println("insert item: "+itemName+" at node: "+nodeIndex)
    val actorRef=system.actorSelection("akka://ChordProtocolHW4/user/node_"+nodeIndex.toString)
    //println(actorRef.pathString)
    val future = actorRef ? PutItemDetail(itemName,itemDetail,nodeIndex)

    val itemInserted = Await.result(future, timeout.duration).asInstanceOf[String]

    return "done"

  }

}