package sapper

import spinal.core._
import spinal.lib.io._

object SapperVhdl {
  def main(args: Array[String]): Unit = {
    SpinalConfig(
      device = Device.XILINX,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT)
    )
      .generateVhdl(InOutWrapper(Sapper(false)))
      .printPruned()
  }
}
