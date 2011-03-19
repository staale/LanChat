package org.staale.lanchat

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import actors.Actor._
import actors.Actor

/**
 * Created by IntelliJ IDEA.
 * User: staaleu
 * Date: 3/19/11
 * Time: 7:58 AM
 * To change this template use File | Settings | File Templates.
 */

class ListenActor extends Actor {
  val messages = new collection.mutable.Stack[Message];

  def act() {
    loop {
      react {
        case "stop" => exit
        case "pop" => reply(if (messages.isEmpty) None else Some(messages.pop))
        case m: Message => messages.push(m)
      }
    }
  }
}

class MulticastMessage extends FlatSpec with ShouldMatchers {

  "A multicast actor" should "publish messages to subscribers" in {
    val sender = new MulticastActor();
    val listenActor = new ListenActor
    listenActor.start()

    sender ! Subscribe(listenActor)
    sender ! Message("Hello world")
    Thread.sleep(200)
    (listenActor !? "pop") should equal(Some(Message("Hello world")))
    listenActor ! "stop"
    sender ! StopTransmission
  }

  "A multicast actor" should "receive messages from other actors" in {
    val src = new MulticastActor()
    val target = new MulticastActor()
    val listenActor = new ListenActor
    listenActor.start()
    
    target ! Subscribe(listenActor)
    src ! Message("Hello world")
    Thread.sleep(500)
    (listenActor !? "pop") should equal(Some(Message("Hello world")))
    listenActor ! "stop"
    src ! StopTransmission
    target ! StopTransmission

  }
}