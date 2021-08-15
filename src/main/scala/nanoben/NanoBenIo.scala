package nanoben

import spinal.core._

import scala.language.postfixOps

case class NanoBenIo() extends Bundle {
  val input_0, input_1: UInt = in UInt (4 bits)
  val output: UInt = out UInt (4 bits)
}
