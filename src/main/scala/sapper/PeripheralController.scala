package sapper

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

import scala.language.postfixOps

case class PeripheralController() extends Component {
  val io = new Bundle {
    val interface = PeripheralInterface()
    val memory = slave(MemoryInterface())
  }

  val addressLow = Reg(Bits(4 bits))
  val addressHigh = Reg(Bits(4 bits))
  val wordLow = Reg(Bits(4 bits))
  val wordHigh = Reg(Bits(4 bits))

  // Run signals through double flip-flop to avoid metastability
  val signalStage: UInt = RegNext(io.interface.inSignal, 0)
  val signal: UInt = RegNext(signalStage, 0)

  val fsm: StateMachine = new StateMachine {
    io.memory.writeEnable := False
    io.interface.outAck := False

    // Address in
    val readAddressLow = new State with EntryPoint
    val ackAddressLow = new State
    val readAddressHigh = new State
    val ackAddressHigh = new State

    // Word in for memory write
    val readWordLow = new State
    val ackWordLow = new State
    val readWordHigh = new State
    val ackWordHigh = new State

    // Reset ack
    val ackReset = new State

    always {
      // Reset signal resets the state machine to start
      when(signal === PeripheralInterface.SignalReset)(goto(ackReset))
    }

    readAck(readAddressLow, ackAddressLow, addressLow, readAddressHigh)
    readAck(readAddressHigh, ackAddressHigh, addressHigh, readWordLow)
    readAck(readWordLow, ackWordLow, wordLow, readWordHigh)
    readAck(readWordHigh, ackWordHigh, wordHigh, readAddressLow)

    def readAck(readState: State, ackState: State, target: Bits, nextState: State): Unit = {
      readState
        .whenIsActive {
          when(signal === PeripheralInterface.SignalWrite) {
            target := io.interface.inNibble
            goto(ackState)
          }
        }

      ackState
        .onEntry(io.interface.outAck := True)
        .whenIsActive {
          io.interface.outAck := True
          when(signal =/= PeripheralInterface.SignalWrite)(goto(nextState))
        }
        .onExit(io.memory.writeEnable := True)
    }

    ackReset
      .onEntry(io.interface.outAck := True)
      .whenIsActive {
        io.interface.outAck := True
        when(signal =/= PeripheralInterface.SignalReset)(goto(readAddressLow))
      }
  }

  io.memory.address := B(addressHigh, addressLow).asUInt
  io.memory.writeWord := B(wordHigh, wordLow)
}
