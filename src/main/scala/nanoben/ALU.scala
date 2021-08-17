package nanoben

import spinal.core._

import scala.language.postfixOps

case class ALU() extends Component {
  val io = new Bundle {
    val inRegister0 = in Bits(8 bits)
    val inRegister1 = in Bits(8 bits)
    /** If true, subtracts instead of adds. */
    val inMode = in Bool()
    val outValue = out Bits(8 bits)
    val outCarry = out Bool()
  }

  // Apply negation to second value if subtracting
  val xorMask = B(8 bits, default -> io.inMode)
  val register1 = io.inRegister1 ^ xorMask

  // Addition with carry filled into the lowest bit
  // This has been optimized for the best LUT usage
  val addInput0 = Bits(9 bits)
  val addInput1 = Bits(9 bits)
  addInput0(8 downto 1) := io.inRegister0
  addInput0(0) := io.inMode
  addInput1(8 downto 1) := register1
  addInput1(0) := io.inMode

  val addOutput = UInt(10 bits)
  addOutput := addInput0.asUInt +^ addInput1.asUInt

  // Unpack the output
  io.outValue := addOutput(8 downto 1).asBits
  io.outCarry := addOutput(9)
}
