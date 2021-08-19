# Sapper

Sapper is an educational system-on-a-chip (SoC) design based on Ben Eater's
[8-Bit Breadboard Computer], written in [SpinalHDL].

[8-bit breadboard computer]: https://eater.net/8bit
[SpinalHDL]: https://github.com/SpinalHDL/SpinalHDL

## Development Board

Sapper can be run on a [Basys 3 Artix-7 FPGA Trainer Board], and this repository contains a
constraints file for this board.
You can use [Vivado ML Standard] to synthesize and upload a generated VHDL file to this board.
Sapper should also work with other boards and FPGAs, but this has not been tested.

[basys 3 artix-7 fpga trainer board]: https://store.digilentinc.com/basys-3-artix-7-fpga-beginner-board-recommended-for-introductory-users/
[vivado ml standard]: https://www.xilinx.com/products/design-tools/vivado.html
