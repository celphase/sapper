package sapper

import spinal.core._

import scala.language.postfixOps

case class Memory() extends Component {
  val io = new Bundle {
    val inAddress = in UInt (8 bits)
    val inValue = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val inPeripheralAddress = in UInt (8 bits)
    val inPeripheralData = in Bits (8 bits)
    val inPeripheralWriteEnable = in Bool()
    val outValue = out Bits (8 bits)
  }

  val memory = Mem(Bits(8 bits), wordCount = 256)
  memory.setTechnology(ramBlock)

  io.outValue := memory.readWriteSync(
    address = io.inAddress,
    data = io.inValue,
    enable = True,
    write = io.inWriteEnable,
    clockCrossing = true
  )

  memory.readWriteSync(
    address = io.inPeripheralAddress,
    data = io.inPeripheralData,
    enable = True,
    write = io.inPeripheralWriteEnable,
    clockCrossing = true
  )
}
