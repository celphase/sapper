package nanoben

import spinal.core._

import scala.language.postfixOps

case class NanoBenTopLevel() extends Component {
  val io = new Bundle {
    val sw = in Bits (13 bits)
    val led = out Bits (8 bits)
  }

  // These correspond directly to the constraints file
  io.sw.setName("sw")
  io.led.setName("led")

  val bus = Bits(8 bits)

  // General purpose registers
  val register0 = Register()
  register0.io.inBus := bus
  register0.io.inWriteEnable := io.sw(11)
  val register1 = Register()
  register1.io.inBus := bus
  register1.io.inWriteEnable := io.sw(12)

  // Bus selection, FPGAs don't always have bus transceivers, so we use a mux instead
  val busSelect = Bits(3 bits)
  busSelect := io.sw(10 downto 8)
  bus := busSelect.mux(
    0 -> io.sw(7 downto 0),
    1 -> register0.io.outValue,
    2 -> register1.io.outValue,
    3 -> (register0.io.outValue.asUInt + register1.io.outValue.asUInt).asBits,
    4 -> IntToBits(0),
    5 -> IntToBits(0),
    6 -> IntToBits(0),
    7 -> IntToBits(0)
  )

  io.led := bus
}
