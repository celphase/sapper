package nanoben

import spinal.core._

import scala.language.postfixOps

case class NanoBenTopLevel(simulation: Boolean) extends Component {
  val io = new Bundle {
    val sw = in Bits (16 bits)
    val led = out Bits (9 bits)
  }

  // These correspond directly to the constraints file
  io.sw.setName("sw")
  io.led.setName("led")

  val wBus = WBus()
  wBus.io.inSelect := io.sw(10 downto 8)
  wBus.io.inSwitches := io.sw(7 downto 0)

  // General purpose & math registers
  val register0 = Register()
  register0.io.inBus := wBus.io.outValue
  register0.io.inWriteEnable := io.sw(11)
  wBus.io.inRegister0 := register0.io.outValue

  val register1 = Register()
  register1.io.inBus := wBus.io.outValue
  register1.io.inWriteEnable := io.sw(12)
  wBus.io.inRegister1 := register1.io.outValue

  // ALU
  val alu = ALU()
  alu.io.inRegister0 := register0.io.outValue
  alu.io.inRegister1 := register1.io.outValue
  alu.io.inMode := io.sw(13)
  wBus.io.inAlu := alu.io.outValue

  // Memory
  val addressRegister = Register()
  addressRegister.io.inBus := wBus.io.outValue
  addressRegister.io.inWriteEnable := io.sw(14)

  val memory = Memory(simulation)
  memory.io.inAddress := addressRegister.io.outValue.asUInt
  memory.io.inValue := wBus.io.outValue
  memory.io.inWriteEnable := io.sw(15)
  wBus.io.inMemory := memory.io.outValue

  // Output to device IO
  io.led(7 downto 0) := wBus.io.outValue
  io.led(8) := alu.io.outCarry
}
