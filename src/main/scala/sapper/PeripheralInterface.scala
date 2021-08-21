package sapper

import spinal.core._
import spinal.lib._
import spinal.lib.io._

import scala.language.postfixOps

case class PeripheralInterface() extends Bundle {
  val inReq = in Bool()
  val outAck = out Bool()
  /** Metadata for this req message, tells the controller what to do with the nibble. */
  val inSignal = in UInt (2 bits)
  val ioNibble = master(TriState(Bits(4 bits)))
}

object PeripheralInterface {
  val SignalReset = 0
  val SignalWrite = 1
  val SignalRead = 2
}
