package sapper

import sapper.Extensions.conversion
import spinal.core._

import scala.language.postfixOps

case class ExecutionCtrl() extends Component {
  val io = new Bundle {
    val inBus = in Bits (8 bits)
    val outSignals = out(ControlSignals())
    val outProgramCounter = out UInt (8 bits)
  }

  // Microcode ROM
  // Instruction: reserved, alu mode, 3 bits bus store, 3 bits bus read
  // 8 slots of microcode per instruction (3 bit micro-step counter)
  val MC_BUS_READ_MEMORY = "0000_0011" b
  val MC_BUS_READ_PROGRAM_COUNTER = "0000_0100" b
  val MC_BUS_WRITE_ADDRESS = "0001_1000" b
  val MC_BUS_WRITE_INSTRUCTION = "0010_1000" b
  val MC_BUS_WRITE_PROGRAM_COUNTER = "0011_0000" b

  // Schedule the instruction at the pointer to be read from memory
  val setMemPc = MC_BUS_READ_PROGRAM_COUNTER | MC_BUS_WRITE_ADDRESS
  // Fetch the read instruction from the memory
  val readInst = MC_BUS_READ_MEMORY | MC_BUS_WRITE_INSTRUCTION

  // Instruction microcode lists
  // All instructions end with fetching a next instruction, which resets the micro-step counter
  val instructions = Seq(
    // 0x0: NOP, no-op
    Seq(setMemPc, readInst),
    // 0x1: LOAD, reg0 = mem[I]
    Seq(setMemPc, readInst),
    // 0x2: STORE, mem[I] = reg0
    Seq(setMemPc, readInst),
    // 0x3: MOV, reg1 = reg0
    Seq(setMemPc, readInst),
    // 0x4: ADD, reg0 = reg0 + reg1
    Seq(setMemPc, readInst),
    // 0x5: SUB, reg0 = reg0 - reg1
    Seq(setMemPc, readInst),
    // 0x6: JMP, jump the program counter to a given location
    Seq(
      setMemPc,
      MC_BUS_READ_MEMORY | MC_BUS_WRITE_PROGRAM_COUNTER,
      setMemPc,
      readInst
    ),
    // 0x7: OUT, set the output register
    Seq(setMemPc, readInst)
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

  // Instruction execution state
  val writeInstruction = Bool()
  val fetchInstruction = Bits(4 bits)
  val fetchMicroStep = UInt(3 bits)

  // The 'current' registers reflect what was looked up at the start of this clock pulse (previous fetch values)
  val currentInstruction = RegNextWhen(fetchInstruction, writeInstruction) init 0
  val currentMicroStep = Reg(UInt(3 bits)) init 0

  // If the instruction is going to get overwritten, go back to 0 for the next micro-step, otherwise increment.
  // Also shortcut fetch instruction for microcode fetching if we're going to overwrite, so that on the next clock tick
  // we can fetch it before the register's set.
  when(writeInstruction) {
    fetchInstruction := io.inBus(3 downto 0)
    fetchMicroStep := 0
  } otherwise {
    fetchInstruction := currentInstruction
    fetchMicroStep := currentMicroStep + 1
  }
  currentMicroStep := fetchMicroStep

  // Fetch the micro-instruction from the ROM
  val microAddress = B(fetchInstruction, fetchMicroStep).asUInt
  val trimmedAddress = microAddress(5 downto 0)
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
    is(6)(programCounter := io.inBus.asUInt)
  }

  io.outSignals.aluMode := signalBits(6)

  // Writing the program counter to the bus will increment it on the next cycle
  when(busSelect === 4)(programCounter := programCounter + 1)
}
