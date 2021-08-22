package sapper

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import spinal.lib.fsm._

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

  // On every byte received, set memory
  val address = Reg(UInt(8 bits)) init 0

  val fsm = new StateMachine {
    uartCtrl.io.read.ready := False
    uartCtrl.io.write.valid := False
    uartCtrl.io.write.payload := B(8 bits, default -> False)
    io.memory.address := address
    io.memory.writeWord := B(8 bits, default -> False)
    io.memory.writeEnable := False

    val readAddress = new State() with EntryPoint
    val readMode = new State()
    val readData = new State()
    val writeData = new State()

    readAddress
      .whenIsActive {
        uartCtrl.io.read.ready := True
        when(uartCtrl.io.read.valid) {
          address := uartCtrl.io.read.payload.asUInt
          goto(readMode)
        }
      }

    readMode
      .whenIsActive {
        uartCtrl.io.read.ready := True
        when(uartCtrl.io.read.valid) {
          switch(uartCtrl.io.read.payload) {
            is(0)(goto(readData))
            is(1)(goto(writeData))
          }
        }
      }

    readData
      .whenIsActive {
        uartCtrl.io.read.ready := True
        when(uartCtrl.io.read.valid) {
          io.memory.writeWord := uartCtrl.io.read.payload
          io.memory.writeEnable := True
          goto(readAddress)
        }
      }
  }
}
