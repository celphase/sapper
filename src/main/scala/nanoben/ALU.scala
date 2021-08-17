package nanoben

import spinal.core._

import scala.language.postfixOps

case class ALU() extends Component {
  val io = new Bundle {
    val inRegister0 = in Bits(8 bits)
    val inRegister1 = in Bits(8 bits)
    val outValue = out Bits(8 bits)
    val outCarry = out Bool()
  }

  val aluOutput = UInt(9 bits)
  aluOutput := io.inRegister0.asUInt +^ io.inRegister1.asUInt

  io.outValue := aluOutput(7 downto 0).asBits
  io.outCarry := aluOutput(8)
}
