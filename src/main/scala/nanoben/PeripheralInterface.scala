package nanoben

import spinal.core._

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
  val ack = Reg(Bool())
  val state = Reg(UInt(2 bits))

  // Run signals through double flip-flop to avoid metastability
  val reqStage = RegNext(io.inReq)
  val req = RegNext(reqStage)
  val resetStage = RegNext(io.inReset)
  val reset = RegNext(resetStage)

  // When reset is given, state goes back to 0
  when(reset.rise) {
    state := 0
    ack := True
  }
  when(reset.fall) {
    ack := False
  }

  // We track what to do sequentially
  when(req.rise) {
    ack := True
    switch(state) {
      is(0) {
        addressLow := io.inNibble
      }
      is(1) {
        addressHigh := io.inNibble
      }
      is(2) {
        wordLow := io.inNibble
      }
      is(3) {
        wordHigh := io.inNibble
      }
    }
  }
  when(req.fall) {
    // This wraps around intentionally to reset
    state := state + 1
    ack := False
  }

  // Delay memory write by one clock cycle
  io.outMemoryWriteEnable := False
  when(ack.rise && state === 3) {
    io.outMemoryWriteEnable := True
  }

  io.outMemoryAddress := B(addressHigh, addressLow).asUInt
  io.outMemoryData := B(wordHigh, wordLow)
  io.outAck := ack
}
