package sapper

import spinal.core._

object SapperVhdl {
  def main(args: Array[String]): Unit = {
    SpinalConfig(
      device = Device.XILINX,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT)
    )
      .generateVhdl(Sapper(false))
      .printPruned()
  }
}
