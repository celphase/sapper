package nanoben

import spinal.core.SpinalVhdl

object NanoBenTopLevelVhdl {
  def main(args: Array[String]): Unit = {
    SpinalVhdl(new NanoBenTopLevel)
  }
}
