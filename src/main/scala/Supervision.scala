import java.util.concurrent.TimeUnit

import Checker.{BlackUser, CheckUser, WhiteUser}
import Recorder.NewUser
import Storage.AddUser
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

/**
  * Created by Hari Rao on 07/03/17.
  *
  * Messages are sent to the actors through one of the following methods
  * Tell(!) - Send a message asynchronously (i.e not wait for the results)
  * Ask(?) - Asks sends a message asynchronously and returns a future representing a possible reply
  */

case class User(username: String, email: String)

object Recorder{
  sealed trait RecorderMsg

  // Recorder Messages
  case class NewUser(user: User) extends RecorderMsg

}

object Checker{
  sealed trait CheckerMsg
  // Checker Messages
  case class CheckUser(user: User) extends CheckerMsg

  sealed trait CheckerResponse
  //Checker Responses
  case class BlackUser(user: User) extends CheckerResponse
  case class WhiteUser(user: User) extends CheckerResponse
}

object Storage{
  sealed trait StorageMsg

  //Storage Message
  case class AddUser(user: User) extends StorageMsg
}

class Storage extends Actor{
  var users = List.empty[User]

  override def receive = {
    case AddUser(user) =>
          println(s"Strage: $user added")
          users = user :: users
  }
}

class Checker extends Actor{
  val blackList = List(User("Hari","harirao@iamblocked.com"))
  override def receive = {
    case CheckUser(user)  if blackList.contains(user) =>
      println(s"Checker: $user in the blacklist")
      sender() ! BlackUser(user)
    case CheckUser(user) =>
      println(s"Checker $user not in the blacklist")
      sender() ! WhiteUser(user)
  }
}

class Recorder(checker: ActorRef, storage: ActorRef) extends Actor{

  //Todo :-  Need to understand why the following import and timeout required. I assume it is the max waiting time of the Ask but should get clarity
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(5,TimeUnit.SECONDS)

  override def receive = {
    case NewUser(user) =>
      //Todo :- Cannot understand why a map is used instead of match-case. Need to investigate
      checker ? CheckUser(user) map{
        case WhiteUser(user) =>
          storage ! AddUser(user)
        case BlackUser(user) =>
          println(s"Recorder: $user in the blacklist")

      }
  }
}

object TalkToActor extends App{
  //Create a talk-to-actor system
  val system = ActorSystem("talk-to-actor")

  //Create the checker Actor
  val checker = system.actorOf(Props[Checker],"checker")

  //Create the storage Actor
  val storage = system.actorOf(Props[Storage],"storage")

  //Create the recorder Actor
  val recorder = system.actorOf(Props(new Recorder(checker,storage)),"recorder")

  //send NewUser Message to Recorder
  recorder ! Recorder.NewUser(User("Hari","harirao@scalapracs.com"))

  recorder ! Recorder.NewUser(User("Hari","harirao@iamblocked.com"))

  Thread.sleep(100)

  //shutdown system
  system.terminate()

}