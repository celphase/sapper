package sapper

import spinal.core._
import spinal.lib.io._

import scala.language.postfixOps

object SapperVhdl {
  def main(args: Array[String]): Unit = {
    SpinalConfig(
      device = Device.XILINX,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT),
      defaultClockDomainFrequency = FixedFrequency(100 MHz)
    )
      .generateVhdl(InOutWrapper(Sapper(false)))
      .printPruned()
  }
}
