package org.staale.lanchat

import util.Marshal
import actors.Actor
import actors.TIMEOUT
import java.net._

sealed trait MulticastActorMessage
case class Subscribe(actor: Actor) extends MulticastActorMessage
case object StopTransmission extends MulticastActorMessage

abstract class MessageBase

class MulticastActor(private val group:InetAddress, private val port:Int) extends Actor {

  def this() = this(InetAddress.getByName("225.11.22.33"), 5678);

  private val socket = new MulticastSocket(port);
  private val packet = new DatagramPacket(new Array[Byte](4096), 4096);

  socket.setSoTimeout(50);
  socket.joinGroup(group);
  start()

  private var listeners: List[Actor] = Nil

  def act() {
    loop {
      try {
        socket.receive(packet)
        if (packet.getLength > 0) {
          val data = packet.getData.slice(packet.getOffset, packet.getOffset + packet.getLength)
          val msg = Marshal.load[Any](packet.getData)
          listeners.foreach(_ ! msg)
        }
      }
      catch {
        case e: SocketTimeoutException => // Ignore timeouts
        case e: ClassNotFoundException => {
          println("Could not unmarshal value, cause: %s" format(e.getMessage))
        }
      }
      reactWithin(25) {
        case TIMEOUT =>
        case StopTransmission => exit
        case Subscribe(actor) => listeners = actor :: listeners;
        case msg:MessageBase => {
          val messageBytes = Marshal.dump(msg)
          socket.send(new DatagramPacket(messageBytes, messageBytes.length, group, socket.getLocalPort))
        }
      }
    }
  }
}