package nanoben

import spinal.core._

import scala.language.postfixOps

class NanoBenTopLevel extends Component {
  case class IO() extends Bundle {
    val input_0, input_1: UInt = in UInt (4 bits)
    val output: UInt = out UInt (4 bits)
  }

  val io: IO = IO()

  // Dummy reg because otherwise there's no clk key
  val unused: Any = Reg(Bool()) init false

  io.output := io.input_0 + io.input_1
}
