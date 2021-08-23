package sapper

import scala.language.implicitConversions

object Extensions {
  implicit def conversion(digits: String) = new BinaryInt(digits)

  class BinaryInt(digits: String) {
    def b = {
      Integer.parseInt(digits.replace("_", ""), 2)
    }
  }
}