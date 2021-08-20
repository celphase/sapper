package sapper

import spinal.core._
import spinal.lib.fsm._

import scala.language.postfixOps

case class PeripheralController() extends Component {
  val io = new Bundle {
    val interface = PeripheralInterface()
    val outMemoryAddress = out UInt (8 bits)
    val outMemoryData = out Bits (8 bits)
    val outMemoryWriteEnable = out Bool()
  }

  val addressLow = Reg(Bits(4 bits))
  val addressHigh = Reg(Bits(4 bits))
  val wordLow = Reg(Bits(4 bits))
  val wordHigh = Reg(Bits(4 bits))

  // Run signals through double flip-flop to avoid metastability
  val reqStage = RegNext(io.interface.inWrite, False)
  val req = RegNext(reqStage, False)
  val resetStage = RegNext(io.interface.inReset, False)
  val reset = RegNext(resetStage, False)

  val fsm: StateMachine = new StateMachine {
    io.outMemoryWriteEnable := False
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
      when(reset)(goto(ackReset))
    }

    readAck(readAddressLow, ackAddressLow, addressLow, readAddressHigh)
    readAck(readAddressHigh, ackAddressHigh, addressHigh, readWordLow)
    readAck(readWordLow, ackWordLow, wordLow, readWordHigh)
    readAck(readWordHigh, ackWordHigh, wordHigh, readAddressLow)

    def readAck(readState: State, ackState: State, target: Bits, nextState: State): Unit = {
      readState
        .whenIsActive {
          when(req) {
            target := io.interface.inNibble
            goto(ackState)
          }
        }

      ackState
        .onEntry(io.interface.outAck := True)
        .whenIsActive {
          io.interface.outAck := True
          when(!req)(goto(nextState))
        }
        .onExit(io.outMemoryWriteEnable := True)
    }

    ackReset
      .onEntry(io.interface.outAck := True)
      .whenIsActive {
        io.interface.outAck := True
        when(!reset)(goto(readAddressLow))
      }
  }

  io.outMemoryAddress := B(addressHigh, addressLow).asUInt
  io.outMemoryData := B(wordHigh, wordLow)
}
