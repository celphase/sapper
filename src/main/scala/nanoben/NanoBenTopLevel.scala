package nanoben

import spinal.core._

import scala.language.postfixOps

case class NanoBenTopLevel() extends Component {
  val io = new Bundle {
    val sw = in Bits (12 bits)
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
  register1.io.inWriteEnable := io.sw(10)

  val busSelect = Bits(2 bits)
  busSelect := io.sw(9 downto 8)
  bus := busSelect.mux(
    0 -> io.sw(7 downto 0),
    1 -> register0.io.outValue,
    2 -> register1.io.outValue,
    3 -> (register0.io.outValue.asUInt + register1.io.outValue.asUInt).asBits
  )

  io.led := bus
}
