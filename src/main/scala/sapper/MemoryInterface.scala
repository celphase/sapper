package sapper

import spinal.core._
import spinal.lib._

import scala.language.postfixOps

case class MemoryInterface() extends Bundle with IMasterSlave {
  val address = UInt (8 bits)
  val writeWord = Bits (8 bits)
  val writeEnable = Bool()
  val readWord = Bits (8 bits)

  override def asMaster(): Unit = {
    out(readWord)
    in(address, writeWord, writeEnable)
  }
}
