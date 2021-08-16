package nanoben

import spinal.core._

import scala.language.postfixOps

class NanoBenTopLevel extends Component {
  val io = new Bundle {
    val input_0, input_1 = in UInt (8 bits)
    val output = out UInt (9 bits)
  }

  // Dummy reg because otherwise there's no clk key
  val unused = Reg(Bool()) init false

  val tmp_0 = UInt(9 bits)
  val tmp_1 = UInt(9 bits)
  tmp_0 := ((7 downto 0) -> io.input_0, default -> false)
  tmp_1 := ((7 downto 0) -> io.input_1, default -> false)
  io.output := tmp_0 + tmp_1
}
