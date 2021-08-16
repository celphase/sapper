package nanoben

import spinal.core._
import spinal.core.sim._

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
        val value = r.nextInt(256)

        dut.io.sw #= value
        dut.clockDomain.waitRisingEdge()

        assert(dut.io.led.toInt == value)
      }
    }
  }
}
