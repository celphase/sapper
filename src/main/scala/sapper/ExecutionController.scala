package sapper

import sapper.Extensions.conversion
import spinal.core._

import scala.language.postfixOps

case class ExecutionController() extends Component {
  val io = new Bundle {
    val inBus = in Bits (8 bits)
    val outSignals = out(ControlSignals())
    val outProgramCounter = out UInt (8 bits)
  }

  // Microcode ROM
  val MC_BUS_SELECT_MEMORY = "0_0000_0011" b
  val MC_BUS_SELECT_PROGRAM_COUNTER = "0_0000_0100" b
  val MC_WRITE_ADDRESS = "0_0100_0000" b
  val MC_STORE_INSTRUCTION = "1_0000_0000" b
  val microcode = Mem(
    Bits(9 bits),
    Seq(
      // Get the memory to read the instruction
      MC_BUS_SELECT_PROGRAM_COUNTER | MC_WRITE_ADDRESS,
      // Instruction should now be on the bus, bring it over to the execution controller
      MC_BUS_SELECT_MEMORY | MC_STORE_INSTRUCTION
    ).map { v => B(v) }
  )

  // Program counter
  val programCounter = Reg(UInt(8 bits)) init 0
  //programCounter := programCounter + 1
  io.outProgramCounter := programCounter

  // Instruction register
  val storeInstruction = Bool()
  val instruction = RegNextWhen(io.inBus(3 downto 0), storeInstruction) init 0

  // If the instruction is stored, we also want to jump forward
  when(storeInstruction.rise) {
    programCounter := programCounter + 1
  }

  // Microcode sub-steps
  val microStep = Reg(UInt(4 bits)) init 0
  microStep := microStep + 1

  // Drive the control signals
  val microAddress = B(instruction, microStep).asUInt
  val trimmedAddress = microAddress(0).asUInt
  val signalBits = microcode.readAsync(trimmedAddress)
  io.outSignals.select := signalBits(2 downto 0).asUInt
  io.outSignals.writeRegister0 := signalBits(3)
  io.outSignals.writeRegister1 := signalBits(4)
  io.outSignals.aluMode := signalBits(5)
  io.outSignals.writeAddressRegister := signalBits(6)
  io.outSignals.writeMemory := signalBits(7)
  storeInstruction := signalBits(8)
}
