package org.staale.lanchat

import actors.TIMEOUT
import java.util.Date
import actors.Actor._

object BroadcastMain {
  def main(args:Array[String]) {
    val multicastOut = new MulticastActor
    actor {
      loop {
        reactWithin(50) {
          case TIMEOUT => multicastOut ! "Time is now %s".format(new Date)
        }
      }
    }
  }
}