package sapper

import spinal.core._

import scala.language.postfixOps

case class WordBus() extends Component {
  val io = new Bundle {
    val inSelect = in UInt (3 bits)
    val inRegister0 = in Bits (8 bits)
    val inRegister1 = in Bits (8 bits)
    val inAlu = in Bits (8 bits)
    val inMemory = in Bits (8 bits)
    val inProgramCounter = in Bits(8 bits)
    val outValue = out Bits (8 bits)
  }

  // Bus selection, FPGAs don't always have bus transceivers, so we use a mux instead
  io.outValue := io.inSelect.mux(
    0 -> io.inRegister0,
    1 -> io.inRegister1,
    2 -> io.inAlu,
    3 -> io.inMemory,
    4 -> io.inProgramCounter,
    5 -> IntToBits(0),
    6 -> IntToBits(0),
    7 -> IntToBits(0)
  )
}
