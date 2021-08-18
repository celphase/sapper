package nanoben

import spinal.core._

import scala.language.postfixOps

case class Memory() extends Component {
  val io = new Bundle {
    val inAddress = in UInt (8 bits)
    val inValue = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val inPeripheral = in Bits (8 bits)
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
  val peripheralClockDomain = ClockDomain.internal("peripheralClock")
  peripheralClockDomain.clock := io.inPeripheral(7)
  peripheralClockDomain.reset := False

  val peripheralClockArea = new ClockingArea(peripheralClockDomain) {
    val select = io.inPeripheral(2 downto 0).asUInt
    val ioNibble = io.inPeripheral(6 downto 3)

    val wordLow = RegNextWhen(ioNibble, select === 0)
    val wordHigh = RegNextWhen(ioNibble, select === 1)
    val addressLow = RegNextWhen(ioNibble, select === 2)
    val addressHigh = RegNextWhen(ioNibble, select === 3)

    // TODO: Tri-state data bus for output
    memory.readWriteSync(
      address = B(addressHigh, addressLow).asUInt,
      data = B(wordHigh, wordLow),
      enable = True,
      write = select === 4,
      clockCrossing = true
    )
  }
}
