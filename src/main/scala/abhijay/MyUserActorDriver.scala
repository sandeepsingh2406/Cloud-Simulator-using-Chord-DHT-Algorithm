package abhijay

import akka.actor.{ActorSystem, Props}


/**
  * Created by avp on 11/24/2016.
  */

object MyUserActorDriver {

  var numberOfUsers: Int = 10;

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("UserActorSystem");
    val user = system.actorOf(Props(new UserActor(1)));
    user ! putMovie("test", "test details");
    user ! getMovie ("test");
  }
}
