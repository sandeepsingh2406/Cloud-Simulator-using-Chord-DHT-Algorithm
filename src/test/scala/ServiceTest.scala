import org.scalatest.FunSuite

/**
  * Created by singsand on 11/25/2016.
  */
class ServiceTest extends FunSuite {

  val thread = new Thread(new Runnable {
    def run() {

      //Creating object of SearchEngine1 class to start the web service
      val inst: Service = new Service()
      inst.method(new Array[String](5))
    }
  })

  //Starting the web service
  thread.start()
  println(s"Waiting 10 seconds for web service to start in another thread. Some output from other class might be visible here.")

  Thread.sleep(10000)
  println(s"Web service has been started in another thread.")
  test("Check if web service is started properly and log is created in the specified file") {

    println("Now calling web service at http://127.0.0.1:8080 and checking response.")


    var url = "http://127.0.0.1:8080/"
    var result = scala.io.Source.fromURL(url).mkString
    val Pattern = "<h1>Welcome to the Cloud Simulator</h1>".r
    val matched_result=Pattern.findFirstIn(result.toString).getOrElse("no match")

    println(result)
    //Get response from web service

    thread.interrupt()

    assert(matched_result.equals("<h1>Welcome to the Cloud Simulator</h1>"))

  }


}
