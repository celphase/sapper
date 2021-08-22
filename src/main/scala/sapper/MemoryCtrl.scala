package sapper

import spinal.core._
import spinal.lib._

import scala.language.postfixOps

case class MemoryCtrl() extends Component {
  val io = new Bundle {
    val main = master(MemoryInterface())
    val debug = master(MemoryInterface())
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

  io.debug.readWord := memory.readWriteSync(
    address = io.debug.address,
    data = io.debug.writeWord,
    enable = True,
    write = io.debug.writeEnable,
    clockCrossing = true
  )
}
