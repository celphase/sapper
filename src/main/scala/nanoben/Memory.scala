package nanoben

import spinal.core._

import scala.language.postfixOps

case class Memory(simulation: Boolean) extends Component {
  val io = new Bundle {
    val inAddress = in UInt (8 bits)
    val inValue = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val inPeripheralClock = in Bool()
    val inPeripheralSelect = in Bits (3 bits)
    val inPeripheralNibble = in Bits (4 bits)
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

  // Peripheral memory access
  val peripheralClockDomain = if (!simulation) {
    ClockDomain(clock = io.inPeripheralClock)
  } else {
    // Workaround for simulation seemingly not working with clock domains
    ClockDomain.external("simPeripheralClock")
  }

  val peripheralClockArea = new ClockingArea(peripheralClockDomain) {
    val wordLow = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 1)
    val wordHigh = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 2)
    val addressLow = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 3)
    val addressHigh = RegNextWhen(io.inPeripheralNibble, io.inPeripheralSelect === 4)
    val writeMemory = io.inPeripheralSelect === 5

    // TODO: Tri-state data bus for output
    memory.readWriteSync(
      address = B(addressHigh, addressLow).asUInt,
      data = B(wordHigh, wordLow),
      enable = True,
      write = writeMemory,
      clockCrossing = true
    )
  }
}
