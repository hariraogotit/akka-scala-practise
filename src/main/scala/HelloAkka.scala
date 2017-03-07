import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Hari Rao on 06/03/17.
  */

// Define Actor Message
case class WhoToGreet(who : String)

// Define Greeter Actor
class Greeter extends Actor{
  override def receive = {
    case WhoToGreet(who) => println(s"Hello $who" )
  }
}

object HelloAkkaScala extends App{

  // Create the hello-akka actor system
  val system = ActorSystem("Hello-Akka")

  //Create the greeter actor
val greeter = system.actorOf(Props[Greeter],"greeter")

  //Send WhoToGreet Message to Actor
  greeter ! WhoToGreet("Akka")

}