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

  // Addition
  val aluOutput = UInt(9 bits)
  aluOutput := (io.inRegister0.asUInt +^ register1.asUInt) + io.inMode.asUInt

  io.outValue := aluOutput(7 downto 0).asBits
  io.outCarry := aluOutput(8)
}
