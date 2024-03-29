package sapper

import spinal.core._
import spinal.lib.io._

import scala.language.postfixOps

object SapperVhdl {
  def main(args: Array[String]): Unit = {
    // Put your code here
    val memory = Seq[Byte](
      // NOP
      0,
      // JMP 0
      6, 0
    ).padTo(256, 0.toByte)

    val config = SpinalConfig(
      device = Device.XILINX,
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT),
      defaultClockDomainFrequency = FixedFrequency(100 MHz)
    )
    val report = config.generateVhdl(InOutWrapper(Sapper(simulation = false, initMemory = memory)))

    report.printPruned()
  }
}
