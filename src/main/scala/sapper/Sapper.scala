package sapper

import spinal.core._

import scala.language.postfixOps

case class Sapper(simulation: Boolean) extends Component {
  val io = new Bundle {
    val sw = in Bits (16 bits)
    val inReq = in Bool()
    val inPReset = in Bool()
    val outAck = out Bool()
    val inNibble = in Bits (4 bits)
    val led = out Bits (9 bits)
  }.setName("")

  val wordBus = WordBus()
  wordBus.io.inSelect := io.sw(10 downto 8)
  wordBus.io.inSwitches := io.sw(7 downto 0)

  // General purpose & math registers
  val register0 = Register()
  register0.io.inBus := wordBus.io.outValue
  register0.io.inWriteEnable := io.sw(11)
  wordBus.io.inRegister0 := register0.io.outValue

  val register1 = Register()
  register1.io.inBus := wordBus.io.outValue
  register1.io.inWriteEnable := io.sw(12)
  wordBus.io.inRegister1 := register1.io.outValue

  // ALU
  val alu = ALU()
  alu.io.inRegister0 := register0.io.outValue
  alu.io.inRegister1 := register1.io.outValue
  alu.io.inMode := io.sw(13)
  wordBus.io.inAlu := alu.io.outValue

  // Memory
  val addressRegister = Register()
  addressRegister.io.inBus := wordBus.io.outValue
  addressRegister.io.inWriteEnable := io.sw(14)

  // If the register is currently being written to, shortcut the bus immediately to the memory address
  // This is necessary because this lets the memory immediately fetch the new data without waiting for the register
  val addressBus = io.sw(14) ? wordBus.io.outValue | addressRegister.io.outValue

  val memory = Memory()
  memory.io.inAddress := addressBus.asUInt
  memory.io.inValue := wordBus.io.outValue
  memory.io.inWriteEnable := io.sw(15)
  wordBus.io.inMemory := memory.io.outValue

  val peripheralInterface = PeripheralInterface()
  peripheralInterface.io.inReq := io.inReq
  peripheralInterface.io.inReset := io.inPReset
  io.outAck := peripheralInterface.io.outAck
  peripheralInterface.io.inNibble := io.inNibble
  memory.io.inPeripheralAddress := peripheralInterface.io.outMemoryAddress
  memory.io.inPeripheralData := peripheralInterface.io.outMemoryData
  memory.io.inPeripheralWriteEnable := peripheralInterface.io.outMemoryWriteEnable

  // Program counter
  val programCounter = Reg(UInt(8 bits)) init 0

  // Increment the counter every clock
  programCounter := programCounter + 1

  // Output to device IO
  io.led(7 downto 0) := wordBus.io.outValue
  io.led(8) := alu.io.outCarry
}
