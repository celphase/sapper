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
        val value_0 = r.nextInt(16)
        val value_1 = r.nextInt(16)

        dut.io.input_0 #= value_0
        dut.io.input_1 #= value_1
        dut.clockDomain.waitRisingEdge()

        assert(dut.io.output.toInt == (value_0 + value_1) % 16, s"$value_0 + $value_1")
      }
    }
  }
}
