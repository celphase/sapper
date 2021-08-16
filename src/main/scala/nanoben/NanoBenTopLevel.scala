package nanoben

import spinal.core._

import scala.language.postfixOps

class NanoBenTopLevel extends Component {
  val io = new Bundle {
    val input_0, input_1: UInt = in UInt (8 bits)
    val output: UInt = out UInt (8 bits)
  }

  // Dummy reg because otherwise there's no clk key
  val unused: Any = Reg(Bool()) init false

  io.output := io.input_0 + io.input_1
}
