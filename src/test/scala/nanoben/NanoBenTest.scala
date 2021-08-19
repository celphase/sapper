package nanoben

import org.scalatest.funsuite.AnyFunSuite
import spinal.core._
import spinal.core.sim._

import scala.language.postfixOps
import scala.util.Random

class NanoBenTest extends AnyFunSuite {
  val compiled = SimConfig
    .withWave
    .addSimulatorFlag("-Wno-MULTIDRIVEN")
    .compile(NanoBen(true))

  test("Peripheral Memory Write") {
    compiled.doSim { dut =>
      dut.clockDomain.forkStimulus(period = 10)
      dut.memory.peripheralClockDomain.forkStimulus(period = 100)
      val r = new Random(1234)

      // Just in case
      dut.io.sw #= 0

      for (_ <- 1 to 10) {
        // Fill memory with the values we want to add
        val value0 = r.nextInt(256)
        val value1 = r.nextInt(256)
        writeToAddress(dut, value0, address = 0x00)
        writeToAddress(dut, value1, address = 0x01)

        // Check from the CPU side that we can read the values onto the bus
        //noinspection RedundantDefaultArgument
        dut.io.sw #= switches(bus = 0x00, addressWrite = true)
        dut.clockDomain.waitSampling()
        dut.io.sw #= switches(select = 4)
        dut.clockDomain.waitSampling()
        assert(dut.io.led.toInt % 256 == value0)

        dut.io.sw #= switches(bus = 0x01, addressWrite = true)
        dut.clockDomain.waitSampling()
        dut.io.sw #= switches(select = 4)
        dut.clockDomain.waitSampling()
        assert(dut.io.led.toInt % 256 == value1)
      }
    }
  }

  test("NanoBen") {
    compiled.doSim { dut => testRegisters(dut) }
    compiled.doSim { dut => testAddition(dut) }
    compiled.doSim { dut => testUnsignedSubtraction(dut) }
  }

  def testRegisters(dut: NanoBen): Unit = {
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

  def testAddition(dut: NanoBen): Unit = {
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

  def testUnsignedSubtraction(dut: NanoBen): Unit = {
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

  def writeToAddress(dut: NanoBen, value: Int, address: Int): Unit = {
    println(s"Writing $value to 0x${address.toHexString}")

    // Write word
    dut.io.JB #= peripheral(nibble = nibl(value), select = 1)
    dut.memory.peripheralClockDomain.waitSampling()
    dut.io.JB #= peripheral(nibble = nibh(value), select = 2)
    dut.memory.peripheralClockDomain.waitSampling()

    // Write memory address
    dut.io.JB #= peripheral(nibble = nibl(address), select = 3)
    dut.memory.peripheralClockDomain.waitSampling()
    dut.io.JB #= peripheral(nibble = nibh(address), select = 4)
    dut.memory.peripheralClockDomain.waitSampling()

    // Submit write
    dut.io.JB #= peripheral(select = 5)
    dut.memory.peripheralClockDomain.waitSampling()
    dut.io.JB #= 0
  }

  def peripheral(nibble: Int = 0, select: Int): Int = {
    nibble << 3 | select
  }

  def nibl(value: Int): Int = {
    value & 0x0F
  }

  def nibh(value: Int): Int = {
    (value >> 4) & 0x0F
  }

  def switches(
                bus: Int = 0,
                select: Int = 0,
                write0: Boolean = false,
                write1: Boolean = false,
                subtract: Boolean = false,
                addressWrite: Boolean = false,
                memWrite: Boolean = false
              ): Int = {
    var switches = bus
    switches |= select << 8
    switches |= write0.toInt << 11
    switches |= write1.toInt << 12
    switches |= subtract.toInt << 13
    switches |= addressWrite.toInt << 14
    switches |= memWrite.toInt << 15
    switches
  }
}
