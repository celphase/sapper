package nanoben

import spinal.core._

import scala.language.postfixOps

case class WBus() extends Component {
  val io = new Bundle {
    val inSelect = in Bits (3 bits)
    val inSwitches = in Bits (8 bits)
    val inRegister0 = in Bits (8 bits)
    val inRegister1 = in Bits (8 bits)
    val inAlu = in Bits (8 bits)
    val outValue = out Bits (8 bits)
  }

  // Bus selection, FPGAs don't always have bus transceivers, so we use a mux instead
  io.outValue := io.inSelect.mux(
    0 -> io.inSwitches,
    1 -> io.inRegister0,
    2 -> io.inRegister1,
    3 -> io.inAlu,
    4 -> IntToBits(0),
    5 -> IntToBits(0),
    6 -> IntToBits(0),
    7 -> IntToBits(0)
  )
}
