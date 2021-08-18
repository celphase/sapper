package nanoben

import spinal.core._

import scala.language.postfixOps

case class Memory(simulation: Boolean) extends Component {
  val io = new Bundle {
    val inAddress = in UInt (8 bits)
    val inValue = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val outValue = out Bits (8 bits)
  }

  val memory = Mem(Bits(8 bits), wordCount = 256)
  if (!simulation) {
    memory.setTechnology(ramBlock)
    memory.generateAsBlackBox()
  }

  io.outValue := memory.readWriteSync(
    address = io.inAddress,
    data = io.inValue,
    enable = True,
    write = io.inWriteEnable
  )
}
