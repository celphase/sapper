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
  // Instruction: reserved, alu mode, 3 bits bus store, 3 bits bus read
  // All instructions end with fetching a new instruction and resetting the micro-step counter
  // 8 slots of microcode per instruction (3 bit micro-step counter)
  val MC_BUS_READ_MEMORY = "0000_0011" b
  val MC_BUS_READ_PROGRAM_COUNTER = "0000_0100" b
  val MC_BUS_WRITE_ADDRESS = "0001_1000" b
  val MC_BUS_WRITE_INSTRUCTION = "0010_1000" b

  // Get the memory to read the instruction
  val readNextI = MC_BUS_READ_PROGRAM_COUNTER | MC_BUS_WRITE_ADDRESS
  // Instruction should now be on the bus, bring it over to the execution controller
  val writeNextI = MC_BUS_READ_MEMORY | MC_BUS_WRITE_INSTRUCTION

  // Instruction microcode lists
  val instructions = Seq(
    // 0x0: NOP
    Seq(readNextI, writeNextI),
    // 0x1: LOAD
    Seq(readNextI, writeNextI),
    // 0x2: STORE
    Seq(readNextI, writeNextI)
  )

  // Convert the microcode lists to microcode ROM, up to 8 instructions per slot
  val padded = instructions.map { v => v.padTo(8, 0) }
  val microcodeRom = Mem(
    Bits(9 bits),
    padded.flatten.map { v => B(v) }
  )

  // Program counter
  val programCounter = Reg(UInt(8 bits)) init 0
  io.outProgramCounter := programCounter

  // Instruction register
  val writeInstruction = Bool()
  val instruction = RegNextWhen(io.inBus(3 downto 0), writeInstruction) init 0

  // Microcode sub-steps
  val currentMicroStep = Reg(UInt(3 bits)) init 0

  // If the instruction got overwritten, go back to 0 for the next micro-step, otherwise increment.
  // Also shortcut fetch instruction for microcode fetching if we're going to overwrite, so that on the next clock tick
  // we can fetch it before the register's set.
  val fetchInstruction = Bits(4 bits)
  val fetchMicroStep = UInt(3 bits)
  when(writeInstruction) {
    fetchInstruction := instruction
    fetchMicroStep := 0
  } otherwise {
    fetchInstruction := io.inBus(3 downto 0)
    fetchMicroStep := currentMicroStep + 1
  }
  currentMicroStep := fetchMicroStep

  // Fetch the micro-instruction from the ROM
  val microAddress = B(fetchInstruction, fetchMicroStep).asUInt
  val trimmedAddress = microAddress(4 downto 0)
  val signalBits = microcodeRom.readSync(
    address = trimmedAddress,
    enable = True
  )

  // Drive the signals for this micro-instruction
  val busSelect = signalBits(2 downto 0).asUInt
  io.outSignals.select := busSelect

  io.outSignals.writeRegister0 := False
  io.outSignals.writeRegister1 := False
  io.outSignals.writeAddressRegister := False
  io.outSignals.writeMemory := False
  writeInstruction := False
  switch(signalBits(5 downto 3)) {
    // 0 is intentionally left no-op
    is(1)(io.outSignals.writeRegister0 := True)
    is(2)(io.outSignals.writeRegister1 := True)
    is(3)(io.outSignals.writeAddressRegister := True)
    is(4)(io.outSignals.writeMemory := True)
    is(5)(writeInstruction := True)
  }

  io.outSignals.aluMode := signalBits(6)

  // Writing the program counter to the bus will increment it on the next cycle
  when(busSelect === 4)(programCounter := programCounter + 1)
}
