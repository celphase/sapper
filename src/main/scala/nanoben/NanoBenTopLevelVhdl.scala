package nanoben

import spinal.core._

object NanoBenTopLevelVhdl {
  def main(args: Array[String]): Unit = {
    SpinalVhdl(new NanoBenTopLevel)
  }
}
