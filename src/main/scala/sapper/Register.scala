package sapper

import spinal.core._

import scala.language.postfixOps

case class Register() extends Component {
  val io = new Bundle {
    val inBus = in Bits (8 bits)
    val inWriteEnable = in Bool()
    val outValue = out Bits (8 bits)
  }

  val data = RegNextWhen(io.inBus, io.inWriteEnable)
  io.outValue := data
}
