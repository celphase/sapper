package nanoben

import spinal.core._

import scala.language.postfixOps

case class PeripheralInterface() extends Component {
  val io = new Bundle {
    val inPeripheralSelect = in Bits (3 bits)
    val inPeripheralNibble = in Bits (4 bits)
    val outMemoryAddress = out UInt (8 bits)
    val outMemoryData = out Bits (8 bits)
    val outMemoryWriteEnable = out Bool()
  }

  // TODO: Tri-state data bus for output
  val wordLow = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 1)
  val wordHigh = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 2)
  val addressLow = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 3)
  val addressHigh = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 4)
  val writeMemory = io.inPeripheralSelect === 5

  io.outMemoryAddress := B(addressHigh, addressLow).asUInt
  io.outMemoryData := B(wordHigh, wordLow)
  io.outMemoryWriteEnable := writeMemory
}
