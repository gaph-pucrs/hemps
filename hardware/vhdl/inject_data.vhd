library STD;
use STD.TEXTIO.all;
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_textio.all;
library work;
use work.hemps_pkg.all;
use work.standards.all;

-------------------------------------------------------------------------------
-- Ports are labelled from the router's perspective
-------------------------------------------------------------------------------
entity inject_data is
  generic(router_nb : integer;
          port_name : string);
  port(
    clock : in std_logic;
    reset : in std_logic;

    clock_rx : out std_logic;
    rx       : out std_logic;
    data_in  : out regflit;
    credit_o : in  std_logic);
end inject_data;


architecture inject_data of inject_data is

  ---arquivos com os pacotes -  pacotes de até 256 flits, e max 100 pacotes por arquivo

  type packet is array (0 to 256) of std_logic_vector(31 downto 0);

  type packet_array is array(0 to 100) of packet;

  signal packets : packet_array;        --- packet read from a file

  signal packet_number : integer := 0;
  --signal dta_out : string(1 to 8);


begin

  clock_rx <= clock;                    -- enable the router to inject data

  ---------------------------------------------------------------------------------------------
  -- inject traffic
  ---------------------------------------------------------------------------------------------
  process
    variable i, ix      : integer;
    variable ARQ_LINE   : line;
    variable line_arq   : string(1 to 2000);
    variable count_flit : integer := 0;
    variable position   : integer := 1;
    file file_out       : text;
    variable fstatus    : file_open_status;
    variable dta_out    : string(1 to 8);
  begin

    rx <= '0';  -- if the file is empty, there is no injection
    ix := 0;

    wait for 200 ns;  -- condition to start injecting the packets (in fact 200 ns is the time required to read the file and restart the injection)

    -- loop to inject all packets described in the input file
    if packet_number > 0 then

      loop
        i := 0;
        while i < CONV_INTEGER(packets(ix)(1) + 2) loop  -- header + packet size

          if credit_o = '1' then        -- flow control
            file_open(fstatus, file_out, "inj" & integer'image(router_nb) & port_name & ".txt", write_mode);
            dta_out := CONV_STRING_32BITS(packets(ix)(i));
            write(ARQ_LINE, dta_out & " " & "," & " " & time'image(NOW));
            writeline(file_out, ARQ_LINE);
            rx      <= '1';
            data_in <= packets(ix)(i);
            wait for 10 ns;
            rx      <= '0';
            wait for 10 ns;
            i       := i + 1;
          else
            wait for 10 ns;
            rx <= '0';
          end if;

        end loop;

        wait for 10 ns;  --  time between packets  - MUDEI MORAES - 07/JULHO 

        ix := ix + 1;                   --   next packet

        exit when ix = packet_number;

      end loop;
    end if;
  end process;

  ---------------------------------------------------------------------------------------------
  -- read the file
  ---------------------------------------------------------------------------------------------
  process
    variable ARQ_LINE   : line;
    variable line_arq   : string(1 to 2000);
    variable count_flit : integer := 0;
    variable position   : integer := 1;
    file file_in        : text;
    variable fstatus    : file_open_status;
  begin

    wait until reset = '1';             -- start reading the file

    file_open(fstatus, file_in, "in" & integer'image(router_nb) & port_name & ".txt", read_mode);

    if fstatus = open_ok then

      assert false report "Reading file in"& integer'image(router_nb) & port_name & ".txt"
                severity note;

      while not (endfile(file_in)) loop  -- one packet per line -----

        readline(file_in, ARQ_LINE);
        read(ARQ_LINE, line_arq(1 to ARQ_LINE'length));  ----  read the line

        report "The line is " & line_arq;

        position   := 1;
        count_flit := 0;                -- flit number
        loop
          for w in 0 to 7 loop
            packets(packet_number)(count_flit)((31-w*4) downto (32-(w+1)*4)) <= CONV_VECTOR(line_arq(position+w to position+w+1), 1);
          end loop;

          position   := position + 9;
          count_flit := count_flit + 1;

          wait for 2 ps;  -- tempo para avançar o processo (tricki!)

          exit when count_flit > 2 and count_flit = (CONV_INTEGER(packets(packet_number)(1)) + 2);  -- second flit is the packet size

        end loop;

        packet_number <= packet_number + 1;  --- number of lines of the traffic file

        wait for 2 ps;                  --required, to wait the next line :-)

      end loop;  -- end loop da linha
    else
      assert false report "Could not open in"& integer'image(router_nb) & port_name & ".txt"
                severity note;
    end if;

  end process;

end inject_data;
