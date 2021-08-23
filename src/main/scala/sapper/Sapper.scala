package sapper

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

import scala.language.postfixOps

case class Sapper(simulation: Boolean) extends Component {
  val io = new Bundle {
    val uart = master(Uart())
  }.setName("")

  val controlSignals = ControlSignals()
  val wordBus = WordBus()
  wordBus.io.inSelect := controlSignals.select

  // General purpose & math registers
  val register0 = RegNextWhen(wordBus.io.outValue, controlSignals.writeRegister0)
  wordBus.io.inRegister0 := register0

  val register1 = RegNextWhen(wordBus.io.outValue, controlSignals.writeRegister1)
  wordBus.io.inRegister1 := register1

  // ALU
  val alu = ALU()
  alu.io.inRegister0 := register0
  alu.io.inRegister1 := register1
  alu.io.inMode := controlSignals.aluMode
  wordBus.io.inAlu := alu.io.outValue

  // Memory
  val addressRegister = RegNextWhen(wordBus.io.outValue, controlSignals.writeAddressRegister)

  // If the register is currently being written to, shortcut the bus immediately to the memory address
  // This is necessary because this lets the memory immediately fetch the new data without waiting for the register
  val addressBus = controlSignals.writeAddressRegister ? wordBus.io.outValue | addressRegister

  val memoryCtrl = MemoryCtrl()
  memoryCtrl.io.main.address := addressBus.asUInt
  memoryCtrl.io.main.writeWord := wordBus.io.outValue
  memoryCtrl.io.main.writeEnable := controlSignals.writeMemory
  wordBus.io.inMemory := memoryCtrl.io.main.readWord

  // Execution controller
  val executionController = ExecutionController()
  executionController.io.inBus := wordBus.io.outValue
  controlSignals := executionController.io.outSignals
  wordBus.io.inProgramCounter := executionController.io.outProgramCounter.asBits

  // Debugging UART
  val debugCtrl = DebugCtrl()
  debugCtrl.io.uart <> io.uart
  debugCtrl.io.memory <> memoryCtrl.io.debug
}
