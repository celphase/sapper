package nanoben

import spinal.core._

class NanoBenTopLevel extends Component {
  val io: NanoBenIo = NanoBenIo()

  // Dummy reg because otherwise there's no clk key
  val unused: Any = Reg(Bool()) init false

  io.output := io.input_0 & io.input_1
}
