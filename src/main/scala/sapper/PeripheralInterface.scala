package sapper

import spinal.core._

import scala.language.postfixOps

case class PeripheralInterface() extends Bundle {
  val inSignal = in UInt (2 bits)
  val outAck = out Bool()
  // TODO: Tri-state data bus for output
  val inNibble = in Bits (4 bits)
}

object PeripheralInterface {
  val SignalNone = 0
  val SignalReset = 1
  val SignalWrite = 2
  val SignalRead = 2
}
