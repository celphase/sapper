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
  val register_0 = Register()
  register_0.io.inBus := bus
  register_0.io.inWriteEnable := io.sw(11)
  val register_1 = Register()
  register_1.io.inBus := bus
  register_1.io.inWriteEnable := io.sw(10)

  val busSelect = Bits(2 bits)
  busSelect := io.sw(9 downto 8)
  bus := busSelect.mux(
    0 -> io.sw(7 downto 0),
    1 -> register_0.io.outValue,
    2 -> register_1.io.outValue,
    3 -> IntToBits(0)
  )

  io.led := bus
}

case class Register() extends Component {
  val io = new Bundle {
    val inBus = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val outValue = out Bits (8 bits)
  }

  val data = RegNextWhen(io.inBus, io.inWriteEnable)
  io.outValue := data
}
