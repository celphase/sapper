package nanoben

import spinal.core._
import spinal.core.sim._

object NanoBenTopLevelTests {
  def main(args: Array[String]): Unit = {
    val compiled = SimConfig
      .withWave
      .compile(new NanoBenTopLevel)

    compiled.doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)

      dut.io.input_0 #= false
      dut.io.input_1 #= false
      dut.clockDomain.waitRisingEdge()
      assert(!dut.io.output.toBoolean)

      dut.io.input_0 #= true
      dut.io.input_1 #= false
      dut.clockDomain.waitRisingEdge()
      assert(!dut.io.output.toBoolean)

      dut.io.input_0 #= true
      dut.io.input_1 #= true
      dut.clockDomain.waitRisingEdge()
      assert(dut.io.output.toBoolean)
    }
  }
}
