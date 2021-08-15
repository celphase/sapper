package nanoben

import spinal.core._

class NanoBenTopLevel extends Component {
  val io = new Bundle {
    val a = in Bool()
    val b = in Bool()
    val c = out Bool()
  }

  io.c := io.a & io.b
}
