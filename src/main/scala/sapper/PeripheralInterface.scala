package sapper

import spinal.core._
import spinal.lib.fsm._

import scala.language.postfixOps

case class PeripheralInterface() extends Component {
  val io = new Bundle {
    val inReq = in Bool()
    val inReset = in Bool()
    val outAck = out Bool()
    // TODO: Tri-state data bus for output
    val inNibble = in Bits (4 bits)
    val outMemoryAddress = out UInt (8 bits)
    val outMemoryData = out Bits (8 bits)
    val outMemoryWriteEnable = out Bool()
  }

  val addressLow = Reg(Bits(4 bits))
  val addressHigh = Reg(Bits(4 bits))
  val wordLow = Reg(Bits(4 bits))
  val wordHigh = Reg(Bits(4 bits))

  // Run signals through double flip-flop to avoid metastability
  val reqStage = RegNext(io.inReq, False)
  val req = RegNext(reqStage, False)
  val resetStage = RegNext(io.inReset, False)
  val reset = RegNext(resetStage, False)

  // This will get overwritten by states
  io.outAck := False

  val fsm: StateMachine = new StateMachine {
    io.outMemoryWriteEnable := False

    val readAddressLow = new State with EntryPoint
    val ackAddressLow = new State
    val readAddressHigh = new State
    val ackAddressHigh = new State
    val readWordLow = new State
    val ackWordLow = new State
    val readWordHigh = new State
    val ackWordHigh = new State

    always {
      // Reset signal resets the state machine to start
      when(reset) {
        io.outAck := True
        goto(readAddressLow)
      }
    }

    readAck(readAddressLow, ackAddressLow, addressLow, readAddressHigh)
    readAck(readAddressHigh, ackAddressHigh, addressHigh, readWordLow)
    readAck(readWordLow, ackWordLow, wordLow, readWordHigh)
    readAck(readWordHigh, ackWordHigh, wordHigh, readAddressLow)

    def readAck(readState: State, ackState: State, target: Bits, nextState: State): Unit = {
      readState
        .whenIsActive {
          when(req) {
            target := io.inNibble
            goto(ackState)
          }
        }

      ackState
        .onEntry(io.outAck := True)
        .whenIsActive {
          when(!req) {
            goto(nextState)
          }
        }
        .onExit(io.outMemoryWriteEnable := True)
    }
  }

  io.outMemoryAddress := B(addressHigh, addressLow).asUInt
  io.outMemoryData := B(wordHigh, wordLow)
}
