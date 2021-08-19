package nanoben

import spinal.core._

object NanoBenVhdl {
  def main(args: Array[String]): Unit = {
    SpinalConfig(device = Device.XILINX)
      .generateVhdl(NanoBen(false))
      .printPruned()
  }
}