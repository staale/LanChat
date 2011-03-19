package org.staale.lanchat

import util.Marshal
import actors.Actor
import actors.Actor._
import actors.TIMEOUT
import java.nio.ByteBuffer
import java.net._

/**
 * Created by IntelliJ IDEA.
 * User: staaleu
 * Date: 3/19/11
 * Time: 8:08 AM
 * To change this template use File | Settings | File Templates.
 */

sealed trait MulticastActorMessage
case class Subscribe(actor: Actor) extends MulticastActorMessage
case object StopTransmission extends MulticastActorMessage

abstract class MessageBase
case class Message(text: String) extends MessageBase

class MulticastActor(private val group:InetAddress) extends Actor {

  def this() = this(InetAddress.getByName("225.11.22.33"));

  private val socket = new MulticastSocket(5678);
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
          println(Class.forName(e.getMessage))
          println(new String(packet.getData.slice(packet.getOffset, packet.getOffset + packet.getLength)))
          e.printStackTrace
        }
      }
      reactWithin(25) {
        case TIMEOUT =>
        case StopTransmission => exit
        case Subscribe(actor) => listeners = actor :: listeners;
        case msg:MessageBase => {
          val messageBytes = Marshal.dump(msg)
          // Verify that the message can be read
          // Marshal.load(messageBytes)
          socket.send(new DatagramPacket(messageBytes, messageBytes.length, group, socket.getLocalPort))
        }
      }
    }
  }
}