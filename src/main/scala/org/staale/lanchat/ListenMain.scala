package org.staale.lanchat

import actors.Actor._

object ListenMain {
  def main(args:Array[String]) {
    val listener = actor {
      loop {
        react {
          case m:String => println(m)
        }
      }
    }

    val multicast = new MulticastActor();
    multicast ! Subscribe(listener)
  }
}