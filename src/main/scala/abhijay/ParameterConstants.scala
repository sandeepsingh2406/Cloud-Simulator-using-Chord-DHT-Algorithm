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
  val duration = 15;

}
