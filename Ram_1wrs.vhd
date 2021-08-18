library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity Ram_1wrs is
  port (
    addr : in STD_LOGIC_VECTOR ( 7 downto 0 );
    clk : in STD_LOGIC;
    wrData : in STD_LOGIC_VECTOR ( 7 downto 0 );
    rdData : out STD_LOGIC_VECTOR ( 7 downto 0 );
    en : in STD_LOGIC;
    wr : in STD_LOGIC;
    mask : in std_logic_vector ( 0 to 0 )
  );
end Ram_1wrs;

architecture STRUCTURE of Ram_1wrs is
  component blk_mem_gen_0 is
  port (
    addra : in STD_LOGIC_VECTOR ( 7 downto 0 );
    clka : in STD_LOGIC;
    dina : in STD_LOGIC_VECTOR ( 7 downto 0 );
    douta : out STD_LOGIC_VECTOR ( 7 downto 0 );
    ena : in STD_LOGIC;
    wea : in STD_LOGIC_VECTOR ( 0 to 0 )
  );
  end component blk_mem_gen_0;
begin
inner: component blk_mem_gen_0
     port map (
      addra(7 downto 0) => addr,
      clka => clk,
      dina => wrData,
      douta => rdData,
      ena => en,
      wea(0) => wr
    );
end STRUCTURE;
