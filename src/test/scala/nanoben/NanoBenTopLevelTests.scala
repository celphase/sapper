package nanoben

import spinal.core._
import spinal.core.sim._

import scala.language.postfixOps
import scala.util.Random

object NanoBenTopLevelTests {
  def main(args: Array[String]): Unit = {
    val compiled = SimConfig
      .withWave
      .compile(NanoBenTopLevel(true))

    compiled.doSim { dut => testRegisters(dut) }
    compiled.doSim { dut => testAddition(dut) }
    compiled.doSim { dut => testUnsignedSubtraction(dut) }
  }

  def testRegisters(dut: NanoBenTopLevel): Unit = {
    dut.clockDomain.forkStimulus(period = 10)
    val r = new Random(1234)

    for (_ <- 1 to 10) {
      val value0 = r.nextInt(256)
      val value1 = r.nextInt(256)

      // Store value in register 0
      dut.io.sw #= switches(value0, write0 = true)
      dut.clockDomain.waitRisingEdge()

      // Store value in register 1
      dut.io.sw #= switches(value1, write1 = true)
      dut.clockDomain.waitRisingEdge()

      // Retrieve and check value in register 0
      dut.io.sw #= switches(select = 1)
      dut.clockDomain.waitRisingEdge()
      var result = dut.io.led.toInt & 0xFF
      assert(result == value0, s"Got $result expected $value0")

      // Retrieve and check value in register 1
      dut.io.sw #= switches(select = 2)
      dut.clockDomain.waitRisingEdge()
      result = dut.io.led.toInt & 0xFF
      assert(result == value1, s"Got $result expected $value1")
    }
  }

  def testAddition(dut: NanoBenTopLevel): Unit = {
    dut.clockDomain.forkStimulus(period = 10)
    val r = new Random(1234)

    for (_ <- 1 to 10) {
      val value0 = r.nextInt(256)
      val value1 = r.nextInt(256)
      val expected = value0 + value1 // % 256 (currently carry's put in the top bit)

      // Store value in register 0
      dut.io.sw #= switches(value0, write0 = true)
      dut.clockDomain.waitRisingEdge()

      // Store value in register 1
      dut.io.sw #= switches(value1, write1 = true)
      dut.clockDomain.waitRisingEdge()

      // Retrieve and check value in ALU
      dut.io.sw #= switches(select = 3)
      dut.clockDomain.waitRisingEdge()
      val result = dut.io.led.toInt
      assert(result == expected, s"Got $result expected $value0 + $value1 = $expected")
    }
  }

  def testUnsignedSubtraction(dut: NanoBenTopLevel): Unit = {
    dut.clockDomain.forkStimulus(period = 10)
    val r = new Random(1234)

    for (_ <- 1 to 10) {
      val gen0 = r.nextInt(256)
      val gen1 = r.nextInt(256)
      val value0 = gen0.max(gen1)
      val value1 = gen0.min(gen1)
      val expected = value0 - value1

      // Store value in register 0
      dut.io.sw #= switches(value0, write0 = true)
      dut.clockDomain.waitRisingEdge()

      // Store value in register 1
      dut.io.sw #= switches(value1, write1 = true)
      dut.clockDomain.waitRisingEdge()

      // Retrieve and check value in ALU
      dut.io.sw #= switches(select = 3, subtract = true)
      dut.clockDomain.waitRisingEdge()
      val result = dut.io.led.toInt % 256 // Ignore the carry overflow
      assert(result == expected, s"Got $result expected $value0 - $value1 = $expected")
    }
  }

  def switches(bus: Int = 0, select: Int = 0, write0: Boolean = false, write1: Boolean = false, subtract: Boolean = false): Int = {
    var switches = bus
    switches |= select << 8
    switches |= write0.toInt << 11
    switches |= write1.toInt << 12
    switches |= subtract.toInt << 13
    switches
  }
}
