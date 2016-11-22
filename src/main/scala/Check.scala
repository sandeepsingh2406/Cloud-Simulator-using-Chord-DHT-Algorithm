import akka.actor.{Actor, ActorRef, ActorSystem, Props}

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

case class AccessActorNode(nodeIndex: Int)

object Check {


  def main(args: Array[String]): Unit = {

    val system = ActorSystem("Check")

    val Master = system.actorOf(Props(new CheckMainActor(2,system)), name = "User_1")
    Master ! "startProcess"
  }
}

class CheckMainActor(TotalNodes: Int ,ChordActorSys: ActorSystem) extends Actor {

  implicit val timeout = Timeout(20 seconds)

  def receive = {

    case AccessActorNode(nodeIndex: Int) =>{
      println("Current Node : "+nodeIndex)
      sender() ! "done for: "+nodeIndex

    }

    case "startProcess" => {
     // println("Start process: "+TotalNodes)

      for (i <- 0 until TotalNodes) {
        println("Inside for loop - instantiating actors for each computer in cloud with node: "+i)
        val workerNodes = ChordActorSys.actorOf(Props(new SecondActor(TotalNodes,i, self)), name = "node_" + i.toString)
        workerNodes ! "intiateEachNode"
      }

      /*val future = self ? "fetchFingerTable"

        val result = Await.result(future, timeout.duration).asInstanceOf[String]*/

//      val eachnodeactor = context.actorSelection("akka://Check/user/node_0")
//      val future = eachnodeactor ? AccessActorNode(0)
//      val result =  Await.result(future, timeout.duration).asInstanceOf[String]

//      println("Node: 0 : result: "+result)

      val eachnodeactor1 = context.actorSelection("akka://Check/user/node_1")
      val future1 = self ? AccessActorNode(1)
      val result1 =  Await.result(future1, timeout.duration).asInstanceOf[String]

      println("Node: 1 : result: "+result1)

      CheckDefinition

      }
    }

  def CheckDefinition: Unit ={

    println("inside def")
    //val temp = callAnotherMethod(currNode)
    //println("inside def after method call: "+temp)
    val eachnodeactor1 = context.actorSelection("akka://Check/user/node_1")
    val future1 = eachnodeactor1 ? AccessActorNode(1)
    val result1 =  Await.result(future1, timeout.duration).asInstanceOf[String]

    println("Node: 1 : result: "+result1)

  }

  def callAnotherMethod(currNode : Int):String ={

    println("inside other call")
    val eachnodeactor = context.actorSelection("akka://Check/user/node_0")
    val future = eachnodeactor ? AccessActorNode(0)
    val result =  Await.result(future, timeout.duration).asInstanceOf[String]

    return result
  }

}

class SecondActor(TotalNodes: Int ,Index: Int, ChordActorSys: ActorRef) extends Actor {

  var succ : Int = -1
  var pred : Int = -1
  var id: Int = Index
  def receive = {
    case "intiateEachNode" => {
      println("Id: "+id)
    }


  }
}