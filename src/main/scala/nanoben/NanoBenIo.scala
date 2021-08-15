package nanoben

import spinal.core._

case class NanoBenIo() extends Bundle {
  val input_0: Bool = in Bool()
  val input_1: Bool = in Bool()
  val output: Bool = out Bool()
}
