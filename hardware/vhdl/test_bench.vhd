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

  type procload is record
    position  : std_logic_vector(31 downto 0);
    status : std_logic;
  end record;

  type list_of_procs is array(0 to MAX_TASKS_APP-1) of procload;


  type config is record
    code_name : string(1 to 40);
    position  : std_logic_vector(31 downto 0);
    code_size : std_logic_vector(31 downto 0);
  end record;

  type list_of_tasks is array(0 to MAX_TASKS_APP-1) of config;
  type list_of_apps is array(0 to APP_NUMBER-1) of string(1 to 40);

  --constant  log_file          : string := "output_master.txt"; --! port description
  constant mlite_description  : string := "RTL";
  constant ram_description    : string := "RTL";
  constant router_description : string := "RTL";

  constant REPO_SIZE     : integer := (MEMORY_SIZE_BYTES/4);  --This math is because each repoline is 32 bits word
  constant TAM_PACKSTART : integer := 4;

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

  impure function get_cfg (cfg_file : in string) return list_of_tasks is

    file file_ptr    : text open read_mode is cfg_file;
    variable inline  : line;
    variable strline : string(1 to 40);
    variable cfg     : list_of_tasks;
    variable str_len : integer;
    variable i       : integer := 0;
  begin

    while not endfile(file_ptr) loop
      readline(file_ptr, inline);
      str_len := inline'length;
      read(inline, strline(1 to inline'length));
      cfg(i).code_name(1 to str_len) := strline(1 to str_len);
      cfg(i).code_name(str_len+1 to 40) := (others => NUL);
      readline(file_ptr, inline);
      hread(inline, cfg(i).position);
      readline(file_ptr, inline);
      hread(inline, cfg(i).code_size);
      i := i + 1;
    end loop;
    file_close(file_ptr);
    return cfg;
  end get_cfg;


  impure function get_procs (cfg_file : in string) return list_of_procs is

    file file_ptr    : text open read_mode is cfg_file;
    variable inline  : line;
    variable strline : string(1 to 40);
    variable procs     : list_of_procs;
    variable cfg     : list_of_tasks;
    variable str_len : integer;
    variable i       : integer := 0;
  begin

   while not endfile(file_ptr) loop
      readline(file_ptr, inline);
      str_len := inline'length;
      read(inline, strline(1 to inline'length));
      cfg(i).code_name(1 to str_len) := strline(1 to str_len);
      cfg(i).code_name(str_len+1 to 40) := (others => NUL);
      readline(file_ptr, inline);
      hread(inline, cfg(i).position);
      procs(i).position := cfg(i).position;
      procs(i).status := '1';
      readline(file_ptr, inline);
      hread(inline, cfg(i).code_size);
      i := i + 1;
    end loop;
    file_close(file_ptr);
    return procs;
  end get_procs;



  impure function get_num_tasks(cfg_file : in string) return integer is

    file file_ptr    : text open read_mode is cfg_file;
    variable inline  : line;
    variable i       : integer := 0;
  begin

    while not endfile(file_ptr) loop
      readline(file_ptr, inline);
      readline(file_ptr, inline);
      readline(file_ptr, inline);
      i := i + 1;
    end loop;
    file_close(file_ptr);
    return i;
  end get_num_tasks;

  impure function get_apps_cfg (cfg_file : in string) return list_of_apps is

    file file_ptr     : text open read_mode is cfg_file;
    variable inline   : line;
    variable strline  : string(1 to 15);
    variable apps_cfg : list_of_apps;
    variable str_len  : integer;
  begin

    for i in 0 to APP_NUMBER-1 loop
      readline(file_ptr, inline);
      str_len := inline'length;
      read(inline, strline(1 to inline'length));
      apps_cfg(i)(1 to str_len) := strline(1 to str_len);
      apps_cfg(i)(str_len+1 to 15) := (others => NUL);
    end loop;
    file_close(file_ptr);
    return apps_cfg;
  end get_apps_cfg;


  impure function next_wave (taloc : in integer; vapp_counter: in integer; vapps_cfg: list_of_apps;  vlisttask_ocupacao: arrayNPe_reg32) return integer is
     variable talocret: integer := 0;
      variable task_cfg          : list_of_tasks;
      variable proc_load         : list_of_procs;
      variable task_num          : integer;    
  begin  
   if (vapp_counter+1 < APP_NUMBER) then
          task_cfg      := get_cfg(vapps_cfg(vapp_counter+1));
          proc_load     := get_procs(vapps_cfg(vapp_counter+1));
          task_num      := get_num_tasks(vapps_cfg(vapp_counter+1));  
       for i in 0 to task_num-1 loop
              for j in 0 to NUMBER_PROCESSORS-1 loop 
                  if (task_cfg(i).position = RouterAddress(j)) then
                      if (vlisttask_ocupacao(j) = x"00000001") then
                          talocret := 1;
                      end if;
                  end if;
              end loop; 
      end loop;
   end if;   

 return talocret; 
  end next_wave;

end;


architecture test_bench of test_bench is

  signal clock     : std_logic := '0';
  signal clock_200 : std_logic := '1';
  signal reset     : std_logic;

  constant apps_cfg   : list_of_apps := get_apps_cfg("apps.cfg");

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
  
  signal task_cfg          : list_of_tasks;
  signal ntask_cfg          : list_of_tasks;
  signal proc_load         : list_of_procs;
  signal task_num          : integer;
  signal ntask_num          : integer;
  signal rd_addr           : std_logic_vector(23 downto 0);
  signal flit_counter      : integer;
  signal file_counter      : integer;
  signal app_counter       : integer;
  signal packstart_counter : integer;
  type packstart_type is array(0 to TAM_PACKSTART-1) of regflit;
  signal packstart_data    : packstart_type;
  signal task_code         : repo_type;

 --- Variaveis novas testbench
  signal listtask_ocupacao_ret  : arrayNPe_reg32:= (others=>x"00000010");
  signal listtask_ocupacao_env  : arrayNPe_reg32:= (others=>x"00000010");
  signal listtask_ocupacao  : arrayNPe_reg32;
  
   --- Variaveis novas testbench Fim 

  type send_state is (WAIT_state, GET_CODE, SEND_TASK, SEND_START);
  signal SEND: send_state;

begin

  packstart_data(0) <= x"00000002";
  packstart_data(1) <= x"00000300";
  packstart_data(2)  <= x"00000000";
  
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



---------------------------------------------------------------------------------------------
-- recebe retornos de encerramento
---------------------------------------------------------------------------------------------

  process
  begin
    wait until (clock_tx_io(0)'event and clock_tx_io(0) = '1') or (reset'event and reset = '1');
    if reset = '1' then
        for i in 0 to NUMBER_PROCESSORS -1 loop
            listtask_ocupacao_ret(i) <=  listtask_ocupacao_ret(i) + x"00000000";
        end loop;  
    else
    	if (data_out_io(0) = x"E0000000") then
         wait until (clock_tx_io(0)'event and clock_tx_io(0) = '1');
         wait until (clock_tx_io(0)'event and clock_tx_io(0) = '1');
         wait until (clock_tx_io(0)'event and clock_tx_io(0) = '1');
         if (data_out_io(0) = x"00000070") then
            wait until (clock_tx_io(0)'event and clock_tx_io(0) = '1');
            for i in 0 to NUMBER_PROCESSORS -1 loop
              if (RouterAddress(i) = data_out_io(0)) then
                listtask_ocupacao_ret(i) <=  listtask_ocupacao_ret(i) + x"00000001";
              end if;
            end loop;  
         end if;
    	end if;
    end if;
  end process;



  reset     <= '1', '0'      after 100 ns;
  -- 100 MHz
  clock     <= not clock     after 5 ns;
  -- 200 MHz
  clock_200 <= not clock_200 after 1.25 ns;

  --Conecta o testbench inicialmente no IO 0
  clock_rx_io(0) <= clock;
  process
    variable  liberar_envio              : integer := 1;
  begin
    wait until (clock'event and clock = '1') or (reset'event and reset = '1');
    if reset = '1' then
      rx_io(0)          <= '0';
      rd_addr           <= (others => '0');
      flit_counter      <= 0; 
      packstart_counter <= 0;
      file_counter      <= 0;
      app_counter       <= 0;
      task_num          <= 0;
      liberar_envio             := 1;
      SEND              <= WAIT_state;
    else 
      for i in 0 to NUMBER_PROCESSORS-1 loop
        listtask_ocupacao(i) <= listtask_ocupacao_env(i) - listtask_ocupacao_ret(i);
      end loop;
      case SEND is
        when WAIT_state =>
        if (liberar_envio = 0) then
             liberar_envio := 1; 
             for i in 0 to ntask_num-1 loop
                    for j in 0 to NUMBER_PROCESSORS-1 loop 
                        if (ntask_cfg(i).position = RouterAddress(j)) then
                            if (listtask_ocupacao(j) = x"00000001") then
                                liberar_envio := 0;
                            end if;
                        end if;
                    end loop; 
             end loop;        
        end if;
        wait until (clock'event and clock = '1');
          if ((app_counter < APP_NUMBER) and (liberar_envio = 1)) then
            liberar_envio    := 0;
            task_cfg      <= get_cfg(apps_cfg(app_counter));
            proc_load     <= get_procs(apps_cfg(app_counter));
            task_num      <= get_num_tasks(apps_cfg(app_counter)); 
            if(app_counter+1 < APP_NUMBER) then
                ntask_cfg      <= get_cfg(apps_cfg(app_counter+1));
                ntask_num      <= get_num_tasks(apps_cfg(app_counter+1));
            end if;
            wait until (clock'event and clock = '1');
            for i in 0 to NUMBER_PROCESSORS-1 loop
              for j in 0 to task_num-1 loop
                if (proc_load(j).position = RouterAddress(i)) then
                  listtask_ocupacao_env(i) <= listtask_ocupacao_env(i) + x"00000001";
                end if;
              end loop; 
            end loop;        
            SEND          <= GET_CODE;

          end if;

        when GET_CODE =>  
          if file_counter < task_num then
            flit_counter  <= CONV_INTEGER(task_cfg(file_counter).code_size);
            task_code     <= load_repo(task_cfg(file_counter).code_name);
            rd_addr       <= (others => '0');
            SEND          <= SEND_TASK;
          else
            app_counter   <= app_counter + 1;
            file_counter  <= 0;
            SEND          <= WAIT_state; 
          end if;

        when SEND_TASK =>  
          if credit_o_io(0) = '1' and flit_counter > 0 then
            rx_io(0) <= '1';
            rd_addr  <= rd_addr + 1;
            if rd_addr = 0 then
              data_in_io(0) <= task_cfg(file_counter).position;
            elsif rd_addr = 1 then
              data_in_io(0) <= task_cfg(file_counter).code_size + 1;
            elsif rd_addr = 2 then
              data_in_io(0) <= x"00000290";
            else
              data_in_io(0) <= task_code(CONV_INTEGER(rd_addr-3));
              flit_counter  <= flit_counter - 1;
            end if;
          elsif flit_counter = 0 then
            SEND      <= SEND_START; 
            rd_addr   <= (others => '0');
            rx_io(0) <= '0';
          end if;

        when SEND_START =>
          if credit_o_io(0) = '1' and  rd_addr < TAM_PACKSTART then 
            rx_io(0) <= '1';
            rd_addr  <= rd_addr + 1;
            if rd_addr = 0 then
              data_in_io(0) <= task_cfg(file_counter).position;
            else
              data_in_io(0) <= packstart_data(CONV_INTEGER(rd_addr-1));
            end if;
          else
            file_counter  <= file_counter + 1;
            rx_io(0)      <= '0';
            SEND          <= GET_CODE;
          end if;

        end case;
    end if;
  end process;

end test_bench;
