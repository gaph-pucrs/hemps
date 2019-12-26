library STD;
use STD.TEXTIO.all;
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_textio.all;
library work;
use work.hemps_pkg.all;
use work.standards.all;

entity receive_data is
  generic(router_nb : integer;
          port_name : string);
  port(
    clock : in std_logic;
    reset : in std_logic;

    clock_tx : in  std_logic;
    tx       : in  std_logic;
    data_out : in  regflit;
    credit_i : out std_logic);
end receive_data;

architecture receive_data of receive_data is
begin


---------------------------------------------------------------------------------------------
-- receive traffic
---------------------------------------------------------------------------------------------

  process(clock_tx)
    variable ARQ_LINE   : line;
    variable line_arq   : string(1 to 2000);
    variable count_flit : integer := 0;
    variable position   : integer := 1;
    file file_out       : text;
    variable fstatus    : file_open_status;
    variable dta_out    : string(1 to 8);

  begin
    credit_i <= '1';                    -- can receive data

    if clock_tx'event and clock_tx = '1' and tx = '1' then
      file_open(fstatus, file_out, "out" & integer'image(router_nb) & port_name & ".txt", write_mode);
      --  report "The output file " & filename & "is open.";
      dta_out := CONV_STRING_32BITS(data_out);
--      report "Data Out is:" & dta_out;
      write(ARQ_LINE, dta_out & " " & "," & " " & time'image(NOW));
      writeline(file_out, ARQ_LINE);
    --write(line_arq, time'image(now));
    --writeline(file_out, line_arq);
    end if;
  end process;

end receive_data;
