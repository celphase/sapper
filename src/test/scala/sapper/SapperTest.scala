package sapper

import org.scalatest.funsuite.AnyFunSuite
import spinal.core._
import spinal.core.sim._

import scala.collection.JavaConverters._
import scala.language.postfixOps
import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

class SapperTest extends AnyFunSuite {
  // Code for testing
  val memory = Seq[Byte](
    // NOP
    0,
    // JMP 0
    6, 0
  ).padTo(256, 0.toByte)

  val compiled = SimConfig
    .withWave
    .addSimulatorFlag("-Wno-MULTIDRIVEN")
    .withConfig(SpinalConfig(
      defaultConfigForClockDomains = ClockDomainConfig(resetKind = BOOT),
      defaultClockDomainFrequency = FixedFrequency(100 MHz)
    ))
    .compile(Sapper(simulation = true, initMemory = memory))

  test("UART Memory Read/Write") {
    compiled.doSim("uart-memory") { dut =>
      waitInitialize(dut)
      val r = new Random(1234)

      for (_ <- 1 to 10) {
        // Fill memory with the values we want to add
        val address = r.nextInt(254) + 2
        val data = r.nextInt(256)
        writeToAddress(dut, address, data)

        // Check that we can read back the value
        val value = readFromAddress(dut, address)
        assert(value == data)
      }
    }
  }

  test("Execution Controller Run") {
    compiled.doSim("execution-run") { dut =>
      waitInitialize(dut)
      // This test exists mainly so we can check the waveform
      dut.clockDomain.waitRisingEdge(10)
    }
  }

  test("Registers") {
    compiled.doSim("registers") { dut =>
      waitInitialize(dut)
      val r = new Random(1234)

      val code =
        """LOAD 3
          |STORE 5
          |LOAD 4
          |STORE 6
          |JMP 0
          |""".stripMargin
      assemble(dut, 7, code)

      for (_ <- 1 to 10) {
        val value0 = r.nextInt(256)
        val value1 = r.nextInt(256)

        println(s"Moving $value0 and $value1")

        // Store the values to be moved
        writeToAddress(dut, 3, value0)
        writeToAddress(dut, 4, value1)
        // 2 intentionally left empty memory slots (5 and 6)

        // Execute (set loop JMP target to start of program)
        writeToAddress(dut, 2, 7)
        // Wait unnecessary, the write address communication already takes up enough time
        writeToAddress(dut, 2, 0)

        // Validate that the new memory addresses have been set correctly
        val result0 = readFromAddress(dut, 5)
        assert(result0 == value0)
        val result1 = readFromAddress(dut, 6)
        assert(result1 == value1)
      }
    }
  }

  // TODO: These tests are disabled until programs can be executed
  /*test("Addition") {
    compiled.doSim { dut =>
      waitInitialize(dut)
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
  }

  test("Unsigned Subtract") {
    compiled.doSim { dut =>
      waitInitialize(dut)
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
  }*/

  def assemble(dut: Sapper, startAddress: Int, code: String): Map[String, Int] = {
    println(s"Assembing and uploading to 0x${startAddress.toHexString}")
    println(code)

    // TODO: This map should contain named addresses
    val addresses = Map[String, Int]()
    var address = startAddress

    val lines = code.lines().iterator().asScala
    for(rawLine <- lines) {
      breakable {
        val line = rawLine.trim()

        if (line.startsWith("//")) {
          break
        }

        val parts = line.split(" +")

        if (parts.isEmpty) {
          break
        }

        val opcode = parts(0).toUpperCase() match {
          case "NOP" => 0
          case "LOAD" => 1
          case "STORE" => 2
          case "MOV" => 3
          case "ADD" => 4
          case "SUB" => 5
          case "JMP" => 6
          case "OUT" => 7
          case _ => 0
        }

        writeToAddress(dut, address, opcode)
        address += 1

        for (part <- parts.drop(1)) {
          val oprand = part.toInt
          writeToAddress(dut, address, oprand)
          address += 1
        }
      }
    }

    addresses
  }

  val baudPeriod = 100000000 / 115200

  def waitInitialize(dut: Sapper): Unit = {
    println("Initializing IO")
    dut.io.uart.rxd #= true

    dut.clockDomain.forkStimulus(period = 10)
    dut.clockDomain.waitRisingEdge()
  }

  def writeToAddress(dut: Sapper, address: Int, value: Int): Unit = {
    println(s"Writing 0x${value.toHexString} to 0x${address.toHexString}")

    writePayload(dut, address)
    writePayload(dut, 0)
    writePayload(dut, value)
  }

  def readFromAddress(dut: Sapper, address: Int): Int = {
    println(s"Reading from 0x${address.toHexString}")

    var result = 0
    val handle = fork {
      result = readPayload(dut)
    }

    writePayload(dut, address)
    writePayload(dut, 1)

    handle.join()
    result
  }

  def writePayload(dut: Sapper, payload: Int): Unit = {
    dut.io.uart.rxd #= false
    dut.clockDomain.waitRisingEdge(baudPeriod)

    (0 to 7).foreach { bitId =>
      dut.io.uart.rxd #= ((payload >> bitId) & 1) != 0
      dut.clockDomain.waitRisingEdge(baudPeriod)
    }

    dut.io.uart.rxd #= true
    dut.clockDomain.waitRisingEdge(baudPeriod * 4)
  }

  def readPayload(dut: Sapper): Int = {
    // Wait for read to be ready to give us data (set to false)
    while (dut.io.uart.txd.toBoolean) {
      dut.clockDomain.waitRisingEdge()
    }

    // Wait till the middle of the sync bit
    dut.clockDomain.waitRisingEdge(baudPeriod / 2)
    if (dut.io.uart.txd.toBoolean) {
      fail("UART frame not started correctly")
    }

    // Wait for the first data bit
    dut.clockDomain.waitRisingEdge(baudPeriod)

    var buffer = 0
    (0 to 7).foreach { bitId =>
      if (dut.io.uart.txd.toBoolean) {
        buffer |= 1 << bitId
      }

      // Wait for next bit
      dut.clockDomain.waitRisingEdge(baudPeriod)
    }

    // Make sure the frame's done
    if (!dut.io.uart.txd.toBoolean) {
      fail("UART frame not ended correctly")
    }

    buffer
  }
}
