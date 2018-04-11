//#full-example
package com.lightbend.akka.sample

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn

object Greeter {
  def props(message: String, printerActor: ActorRef): Props = Props(new Greeter(message, printerActor))
  
  final case class WhoToGreet(who: String)
  case object Greet
  case class Reply(msg: String)
}

class Greeter(message: String, printerActor: ActorRef) extends Actor {
  import Greeter._
  import Printer._

  var greeting = ""

  def receive = {
    case WhoToGreet(who) =>
      /*
        - timeout will be changed into an exception in the future
        - if the reply is not of the same class, class cast exception will be thrown
        - if Status.Failure(Throwable) is sent back, it will also appear in the failure callback of future
        - if the callee of the message throws an exception, the message will be timed out. The cause of exception will not propagate back to the caller.
        if pipeTo is used
        - timeout -> akka.actor.Status.Failure(TimeoutException)
        - class cast exception -> akka.actor.Status.Failure(ClassCastException)
        - Status.Failure(e) -> akka.actor.Status.Failure(Throwable)
        - Reply(s) -> Reply(s)
       */
      greeting = s"$message, $who"
      sender() ! Reply(greeting)
    case Greet           =>
      printerActor ! Greeting(greeting)
  }
}

object Printer {
  def props: Props = Props[Printer]
  final case class Greeting(greeting: String)
}

class Printer extends Actor with ActorLogging {

  def receive = {
    case r =>
      log.info(s"received ${r.getClass.getCanonicalName}")
      log.info(s"received ${r.toString}")
  }
}

object AkkaQuickstart extends App {
  import Greeter._
  import akka.pattern.{ask, pipe}

  val system: ActorSystem = ActorSystem("helloAkka")

  try {
    val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

    val howdyGreeter: ActorRef =
      system.actorOf(Greeter.props("Howdy", printer), "howdyGreeter")

    implicit val timeout = akka.util.Timeout(1 second)
    (howdyGreeter ? WhoToGreet("Akka")).mapTo[Reply].pipeTo(printer)

    println(">>> Press ENTER to exit <<<")
    StdIn.readLine()
  } finally {
    system.terminate()
  }
}
