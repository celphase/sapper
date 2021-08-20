package sapper

import spinal.core._
import spinal.lib._

import scala.language.postfixOps

case class Memory() extends Component {
  val io = new Bundle {
    val main = master(MemoryInterface())
    val peripheral = master(MemoryInterface())
  }

  val memory = Mem(Bits(8 bits), wordCount = 256)
  memory.setTechnology(ramBlock)

  io.main.readWord := memory.readWriteSync(
    address = io.main.address,
    data = io.main.writeWord,
    enable = True,
    write = io.main.writeEnable,
    clockCrossing = true
  )

  io.peripheral.readWord := memory.readWriteSync(
    address = io.peripheral.address,
    data = io.peripheral.writeWord,
    enable = True,
    write = io.peripheral.writeEnable,
    clockCrossing = true
  )
}
