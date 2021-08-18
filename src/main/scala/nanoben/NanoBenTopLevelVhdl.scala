package nanoben

import spinal.core._

object NanoBenTopLevelVhdl {
  def main(args: Array[String]): Unit = {
    SpinalConfig(device = Device.XILINX)
      .generateVhdl(NanoBenTopLevel(false))
  }
}
