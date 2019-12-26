------------------------------------------------------------------------------------------------
--
--  DISTRIBUTED HEMPS  - version 5.0
--
--  Research group: GAPH-PUCRS    -    contact   fernando.moraes@pucrs.br
--
--  Distribution:  September 2013
--
--  Source name:  UartFile.vhd
--
--  AUTHOR: Everton Alceu Carara (everton.carara@pucrs.br)
--
--  DATE CREATED: 2/12/2009
--
--  Brief description:  Writes strings from Echo() and puts() to the log_file.
--
------------------------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_misc.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_textio.all;
use ieee.std_logic_unsigned.all;
use std.textio.all;
use work.mlite_pack.all;

entity UartFile is
  generic(log_file : string := "UNUSED");
  port(
    reset   : in std_logic;
    data_av : in std_logic;
    data_in : in std_logic_vector(31 downto 0));
end;

architecture logic of UartFile is

begin

  process(data_av, reset)
    file store_file      : text open write_mode is log_file;
    variable file_line   : line;
    variable line_type   : character;
    variable line_length : natural := 0;
    variable str         : string (1 to 4);
    variable str_end     : boolean;
  begin
    if reset = '1' then
      str_end := false;
    elsif rising_edge(data_av) then
      str(4)  := character'val(conv_integer(data_in(7 downto 0)));
      str(3)  := character'val(conv_integer(data_in(15 downto 8)));
      str(2)  := character'val(conv_integer(data_in(23 downto 16)));
      str(1)  := character'val(conv_integer(data_in(31 downto 24)));
      str_end := false;
      for i in 1 to 4 loop
        -- Writes a string in the line
        if str(i) /= nul and str(i) /= lf and not str_end then
          write(file_line, str(i));
        -- Detects the string end
        elsif str(i) = nul then
          str_end := true;
        elsif str(i) = lf then
          writeline(store_file, file_line);
        end if;
      end loop;
    end if;
  end process;
end;
