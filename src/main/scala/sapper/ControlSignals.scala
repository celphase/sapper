package sapper

import spinal.core._

import scala.language.postfixOps

case class ControlSignals() extends Bundle {
  val select = UInt(3 bits)
  val writeRegister0 = Bool()
  val writeRegister1 = Bool()
  val aluMode = Bool()
  val writeAddressRegister = Bool()
  val writeMemory = Bool()
}
