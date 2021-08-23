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
  // Instruction: reserved, alu mode, 3 bits STORE, 3 bits SELECT
  // All instructions end with fetching a new instruction and resetting the micro step counter
  // 8 slots of microcode per instruction
  val MC_BUS_READ_MEMORY = "0000_0011" b
  val MC_BUS_READ_PROGRAM_COUNTER = "0000_0100" b
  val MC_BUS_WRITE_ADDRESS = "0001_1000" b
  val MC_BUS_WRITE_INSTRUCTION = "0010_1000" b
  val microcode = Mem(
    Bits(9 bits),
    Seq(
      // Get the memory to read the instruction
      MC_BUS_READ_PROGRAM_COUNTER | MC_BUS_WRITE_ADDRESS,
      // Instruction should now be on the bus, bring it over to the execution controller
      MC_BUS_READ_MEMORY | MC_BUS_WRITE_INSTRUCTION
    ).map { v => B(v) }
  )

  // Program counter
  val programCounter = Reg(UInt(8 bits)) init 0
  io.outProgramCounter := programCounter

  // Instruction register
  val storeInstruction = Bool()
  val instruction = RegNextWhen(io.inBus(3 downto 0), storeInstruction) init 0

  // Microcode sub-steps
  val microStep = Reg(UInt(3 bits)) init 0
  microStep := microStep + 1

  // Fetch the micro-instruction from the ROM
  val microAddress = B(instruction, microStep).asUInt
  val trimmedAddress = microAddress(0).asUInt
  val signalBits = microcode.readAsync(trimmedAddress)

  // Drive the signals for this micro-instruction
  val busSelect = signalBits(2 downto 0).asUInt
  io.outSignals.select := busSelect

  io.outSignals.writeRegister0 := False
  io.outSignals.writeRegister1 := False
  io.outSignals.writeAddressRegister := False
  io.outSignals.writeMemory := False
  storeInstruction := False
  switch(signalBits(5 downto 3)) {
    // 0 is intentionally left no-op
    is(1)(io.outSignals.writeRegister0 := True)
    is(2)(io.outSignals.writeRegister1 := True)
    is(3)(io.outSignals.writeAddressRegister := True)
    is(4)(io.outSignals.writeMemory := True)
    is(5)(storeInstruction := True)
  }

  io.outSignals.aluMode := signalBits(6)

  // Writing the program counter to the bus will increment it on the next cycle
  when(busSelect === 4) {
    programCounter := programCounter + 1
  }
}
