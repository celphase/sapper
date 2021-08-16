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
        dut.io.sw #= switches(value_0, write_0 = true)
        dut.clockDomain.waitRisingEdge()

        // Store value in register 1
        dut.io.sw #= switches(value_1, write_1 = true)
        dut.clockDomain.waitRisingEdge()

        // Retrieve and check value in register 0
        dut.io.sw #= switches(select = 1)
        dut.clockDomain.waitRisingEdge()
        var result = dut.io.led.toInt
        assert(result == value_0, s"Got $result expected $value_0")

        // Retrieve and check value in register 1
        dut.io.sw #= switches(select = 2)
        dut.clockDomain.waitRisingEdge()
        result = dut.io.led.toInt
        assert(result == value_1, s"Got $result expected $value_1")
      }
    }
  }

  def switches(bus: Int = 0, select: Int = 0, write_0: Boolean = false, write_1: Boolean = false): Int = {
    var switches = bus
    switches |= select << 8
    switches |= write_0.toInt << 11
    switches |= write_1.toInt << 10
    switches
  }
}
