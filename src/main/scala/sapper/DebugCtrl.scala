package sapper

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

import scala.language.postfixOps

case class DebugCtrl() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
    val memory = slave(MemoryInterface())
  }

  // Set up the controller
  val uartCtrl = UartCtrl(UartCtrlInitConfig(
    baudrate = 115200,
    dataLength = 7,
    parity = UartParityType.NONE,
    stop = UartStopType.ONE
  ))
  uartCtrl.io.uart <> io.uart

  // We're not sending out data right now
  uartCtrl.io.write.valid := False
  uartCtrl.io.write.payload := B(8 bits, default -> False)

  // On every byte received, set memory
  val address = Reg(UInt(8 bits)) init 0
  io.memory.address := address
  io.memory.writeWord := B(8 bits, default -> False)
  io.memory.writeEnable := False

  val awaitData = Reg(Bool, False)

  uartCtrl.io.read.ready := True
  when(uartCtrl.io.read.valid.rise) {
    when(!awaitData) {
      address := uartCtrl.io.read.payload.asUInt
      awaitData := True
    } otherwise {
      io.memory.writeWord := uartCtrl.io.read.payload
      io.memory.writeEnable := True
      awaitData := False
    }
  }
}
