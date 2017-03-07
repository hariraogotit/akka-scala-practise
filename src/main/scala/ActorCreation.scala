import MusicController.{Play, Stop}
import MusicPlayer.{StartMusic, StopMusic}
import akka.actor.{Actor, ActorSystem, Props}

/**
  * Created by Hari Rao on 06/03/17.
  */

// Music Controller Messages
object MusicController{
  sealed trait ControllerMsg
  case object Play extends ControllerMsg
  case object Stop extends ControllerMsg

  // Recommended practice here is defining the method Props in the companion object
  def props = Props[MusicController]
}

// Music Controller
class MusicController extends Actor{
  override def receive = {
    case Play => println("Music started .....")
    case Stop => println("Music stopped.....")
  }
}

// Music Player Messages
object MusicPlayer{
  sealed trait PlayMsg
  case object StopMusic extends PlayMsg
  case object StartMusic extends PlayMsg
}

// Music Player
class MusicPlayer extends Actor{
  override def receive = {
    case StopMusic => println("I dont want to stop music.")
    case StartMusic =>
      // Declaring one actor in another is dangerous and breaks Actor encapsulation.
      // Never pass Actor reference in to Props. So commented the following.
      // val controller = context.actorOf(Props[MusicController], "musiccontroller" )
      val controller = context.actorOf(MusicController.props, "musiccontroller" )
      controller ! Play
    case _ => println("Unknown Message")
  }
}

object Creation extends App{
  // Create the creation actor system
  val system = ActorSystem("creation")

  //Create the MusicPlayer actor
  val player = system.actorOf(Props[MusicPlayer],"musicplayer")

  //Send StartMusic message to actor
  player ! StartMusic
}