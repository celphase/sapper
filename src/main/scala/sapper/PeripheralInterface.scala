package sapper

import spinal.core._

import scala.language.postfixOps

case class PeripheralInterface() extends Bundle {
  val inWrite = in Bool()
  val inReset = in Bool()
  val outAck = out Bool()
  // TODO: Tri-state data bus for output
  val inNibble = in Bits (4 bits)
}
