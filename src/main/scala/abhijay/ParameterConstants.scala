package abhijay

/**
  * Created by avp on 11/24/2016.
  */
object ParameterConstants {

  val nodeNamePrefix = "node";
  val userNamePrefix = "user";
  val userActorName = "UserActorSystem";
  val userActorNamePrefix = "akka://" + userActorName + "/user/";
  var numberOfUsers: Int = 10;
  var movieDatabaseFile = "movies.txt";
  // simulation duration
  var duration = 15;
  var ratio = "4:1"
  var minRequests = 0;
  var maxRequests = 15;
  var interval = 2;
}
