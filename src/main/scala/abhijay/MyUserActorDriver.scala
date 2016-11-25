package abhijay

import akka.actor.{ActorSystem, Props}


/**
  * Created by avp on 11/24/2016.
  */

object MyUserActorDriver {

  var numberOfUsers: Int = 10;
  var movieDatabaseFile = "movies.txt";

  def main(args: Array[String]): Unit = {

    val actorSystem = ActorSystem("UserActorSystem");
    val user = actorSystem.actorOf(Props(new UserActor(0)));
    user ! putMovie("test", "test details", movieDatabaseFile);
    user ! getMovie ("test");

    instantiateActors(numberOfUsers, actorSystem);



  }

  def instantiateActors(numberOfActors: Int, actorSystem: ActorSystem): Unit = {
    for(i <- 1 until numberOfActors){
      var user = actorSystem.actorOf(Props(new UserActor(i)));
    }
  }
}
