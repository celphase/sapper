# Sapper

Sapper is an educational system-on-a-chip (SoC) design based on Ben Eater's
[8-Bit Breadboard Computer], written in [SpinalHDL].

[8-bit breadboard computer]: https://eater.net/8bit
[spinalhdl]: https://github.com/SpinalHDL/SpinalHDL

## Unfinished

Not all instructions have been implemented!

- NOP: Fully implemented
- LOAD: Only loads to register 0
- STORE: Only stores from register 0
- MOV: Intended to move values between registers, currently no-op
- ADD: ALU exists, but instruction is currently no-op
- SUB: ALU exists, but instruction is currently no-op
- JMP: Only jumps to fixed addresses
- OUT: Currently no-op

Conditional jump also currently is not implemented.

## Development Board

Sapper can be run on a [Basys 3 Artix-7 FPGA Trainer Board], and this repository contains a
constraints file for this board.
You can use [Vivado ML Standard] to synthesize and upload a generated VHDL file to this board.
Sapper should also work with other boards and FPGAs, but this has not been tested.

[basys 3 artix-7 fpga trainer board]: https://store.digilentinc.com/basys-3-artix-7-fpga-beginner-board-recommended-for-introductory-users/
[vivado ml standard]: https://www.xilinx.com/products/design-tools/vivado.html
