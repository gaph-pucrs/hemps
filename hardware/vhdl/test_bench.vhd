------------------------------------------------------------------------------------------------
--
--  DISTRIBUTED HEMPS  - version 5.0
--
--  Research group: GAPH-PUCRS    -    contact   fernando.moraes@pucrs.br
--
--  Distribution:  September 2013
--
--  Source name:  test_bench.vhd
--
--  Brief description:  Test bench.
--
------------------------------------------------------------------------------------------------

library IEEE;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_textio.all;
use std.textio.all;
use work.hemps_pkg.all;
use work.standards.all;

entity test_bench is

  type config is record
    code_name : string(1 to 34);
    position  : std_logic_vector(31 downto 0);
    code_size : std_logic_vector(31 downto 0);
  end record;

  type list_of_apps is array(0 to APP_NUMBER-1) of config;

  --constant  log_file          : string := "output_master.txt"; --! port description
  constant mlite_description  : string := "RTL";
  constant ram_description    : string := "RTL";
  constant router_description : string := "RTL";

  constant REPO_SIZE     : integer := (TOTAL_REPO_SIZE_BYTES/4);  --This math is because each repoline is 32 bits word
  constant TAM_PACKSTART : integer := 13;

  type repo_type is array(REPO_SIZE-1 downto 0) of std_logic_vector(31 downto 0);

  impure function load_repo (repo_file : in string) return repo_type is
    file file_ptr   : text open read_mode is repo_file;
    variable inline : line;
    variable i      : integer := 0;

    variable mem : repo_type := (others => (others => '0'));

  begin
    while not endfile(file_ptr) loop
      if (i = REPO_SIZE) then
        assert false report "ERROR: reposiotory access overflow - i= " & integer'image(i)
          severity error;
      end if;

      readline(file_ptr, inline);
      hread(inline, mem(i));
      i := i + 1;
    end loop;

    file_close(file_ptr);
    return mem;
  end load_repo;

  impure function get_cfg (cfg_file : in string) return list_of_apps is

    file file_ptr    : text open read_mode is cfg_file;
    variable inline  : line;
    variable strline : string(1 to 34);
    variable cfg     : list_of_apps;
  begin

    for i in 0 to APP_NUMBER-1 loop
      readline(file_ptr, inline);
      read(inline, strline(1 to inline'length));
      cfg(i).code_name := strline;
      readline(file_ptr, inline);
      hread(inline, cfg(i).position);
      readline(file_ptr, inline);
      hread(inline, cfg(i).code_size);
    end loop;
    file_close(file_ptr);
    return cfg;
  end get_cfg;

end;

architecture test_bench of test_bench is

  signal clock     : std_logic := '0';
  signal clock_200 : std_logic := '1';
  signal reset     : std_logic;

  constant app_cfg : list_of_apps := get_cfg("apps.cfg");

  signal current_time : integer := 0;
  signal app_i        : integer := 0;

  --NoC Interface (IO)
  signal tx_io       : std_logic_vector((IO_NUMBER-1) downto 0);
  signal data_out_io : arrayNio_regflit;
  signal credit_i_io : std_logic_vector((IO_NUMBER-1) downto 0);
  signal clock_tx_io : std_logic_vector((IO_NUMBER-1) downto 0);
  signal rx_io       : std_logic_vector((IO_NUMBER-1) downto 0);
  signal data_in_io  : arrayNio_regflit;
  signal credit_o_io : std_logic_vector((IO_NUMBER-1) downto 0);
  signal clock_rx_io : std_logic_vector((IO_NUMBER-1) downto 0);

  --Leitura para IO
  signal rd_addr           : std_logic_vector(23 downto 0);
  signal flit_counter      : integer;
  signal file_counter      : integer;
  signal packstart_counter : integer;
  type packstart_type is array(0 to TAM_PACKSTART-1) of regflit;
  signal packstart_data    : packstart_type;
  signal app_code          : repo_type;

begin

  --packstart_data(12) <= x"00000101";
  packstart_data(11) <= x"0000000B";
  packstart_data(10) <= x"00000300";
  packstart_data(9)  <= x"00000000";
  packstart_data(8)  <= x"00000000";
  packstart_data(7)  <= x"00000000";
  packstart_data(6)  <= x"00000000";
  packstart_data(5)  <= x"00000000";
  packstart_data(4)  <= x"00000000";
  packstart_data(3)  <= x"00000000";
  packstart_data(2)  <= x"00000000";
  packstart_data(1)  <= x"00000000";
  packstart_data(0)  <= x"00000000";

  --
  --  HeMPS instantiation 
  --
  HeMPS : entity work.HeMPS
    generic map(
      mlite_description  => mlite_description,
      ram_description    => ram_description,
      router_description => router_description
      )
    port map(
      clock        => clock,
      reset        => reset,
      --repository
      repo_address => open,
      repo_data    => (others => '0'),
      ack_app      => open,
      req_app      => (others => '0'),

      --NoC Interface (IO)
      tx_io       => tx_io,
      data_out_io => data_out_io,
      credit_i_io => credit_i_io,
      clock_tx_io => clock_tx_io,
      rx_io       => rx_io,
      data_in_io  => data_in_io,
      credit_o_io => credit_o_io,
      clock_rx_io => clock_rx_io
      );

  --data_injector : entity work.inject_data
  --generic map (
  --  router_nb => 1,
  --  port_name => "IO"
  --)
  --port map (
  --  clock    => clock,
  --  reset    => reset,
  --  clock_rx => clock_rx_io,
  --  rx       => rx_io,
  -- data_in  => data_in_io,
  -- credit_o => credit_o_io
  --);

  data_consumer : entity work.receive_data
    generic map (
      router_nb => 1,
      port_name => "IO"
      )
    port map (
      clock    => clock,
      reset    => reset,
      clock_tx => clock_tx_io(0),
      tx       => tx_io(0),
      data_out => data_out_io(0),
      credit_i => credit_i_io(0)
      );


  reset     <= '1', '0'      after 100 ns;
  -- 100 MHz
  clock     <= not clock     after 5 ns;
  -- 200 MHz
  clock_200 <= not clock_200 after 1.25 ns;

  --Conecta o testbench inicialmente no IO 0
  clock_rx_io(0) <= clock;
  process (clock, reset)
  begin
    if reset = '1' then
      rx_io(0)          <= '0';
      rd_addr           <= (others => '0');
      flit_counter      <= 0;
      packstart_counter <= 0;
      file_counter      <= 0;
    elsif rising_edge(clock) then
      if credit_o_io(0) = '1' then
        if flit_counter > 0 then
          rx_io(0) <= '1';
          rd_addr  <= rd_addr + 1;
          if rd_addr = 0 then
            data_in_io(0) <= app_cfg(file_counter).position;
          elsif rd_addr = 1 then
            data_in_io(0) <= app_cfg(file_counter).code_size;
            file_counter  <= file_counter + 1;
          elsif rd_addr = 2 then
            data_in_io(0) <= x"00000290";
          else
            data_in_io(0) <= app_code(CONV_INTEGER(rd_addr-3));
            flit_counter  <= flit_counter - 1;
          end if;
        else
          if(file_counter > 0 and packstart_counter > 0) then
            rx_io(0)          <= '1';
            packstart_counter <= packstart_counter - 1;
            data_in_io(0)     <= packstart_data(packstart_counter-1);
          else
            rx_io(0) <= '0';
            rd_addr  <= (others => '0');
            if file_counter < APP_NUMBER then
              app_code                        <= load_repo(app_cfg(file_counter).code_name);
              flit_counter                    <= CONV_INTEGER(app_cfg(file_counter).code_size);
              packstart_counter               <= TAM_PACKSTART;
              packstart_data(TAM_PACKSTART-1) <= app_cfg(file_counter).position;
            end if;
          end if;
        end if;
      end if;
    end if;
  end process;

end test_bench;



