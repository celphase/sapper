package nanoben

import spinal.core._
import spinal.core.sim._

import scala.language.postfixOps
import scala.util.Random

object NanoBenTopLevelTests {
  def main(args: Array[String]): Unit = {
    val compiled = SimConfig
      .withWave
      .compile(new NanoBenTopLevel)

    compiled.doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)
      val r = new Random(1234)

      for (_ <- 1 to 10) {
        val value_0 = r.nextInt(256)
        val value_1 = r.nextInt(256)

        // Store value in register 0
        var switches = value_0
        switches |= 1 << 11
        dut.io.sw #= switches
        dut.clockDomain.waitRisingEdge()

        // Store value in register 1
        switches = value_1
        switches |= 1 << 10
        dut.io.sw #= switches
        dut.clockDomain.waitRisingEdge()

        // Retrieve and check value in register 0
        switches = 1 << 8
        dut.io.sw #= switches
        dut.clockDomain.waitRisingEdge()
        var result = dut.io.led.toInt
        assert(result == value_0, s"Got $result expected $value_0")

        // Retrieve and check value in register 1
        switches = 2 << 8
        dut.io.sw #= switches
        dut.clockDomain.waitRisingEdge()
        result = dut.io.led.toInt
        assert(result == value_1, s"Got $result expected $value_1")
      }
    }
  }
}
