------------------------------------------------------------------------------------------------
--
--  DISTRIBUTED HEMPS  - version 5.0
--
--  Research group: GAPH-PUCRS    -    contact   fernando.moraes@pucrs.br
--
--  Distribution:  September 2013
--
--  Source name:  HeMPS.vhd
--
--  Brief description:  NoC generation
--
------------------------------------------------------------------------------------------------

library IEEE;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use work.standards.all;
use work.hemps_pkg.all;

entity HeMPS is                         -- Interface com o ambiente exterior
  port(
    clock : in std_logic;
    reset : in std_logic;

    -- Tasks repository interface
    repo_address : out std_logic_vector(29 downto 0);
    repo_data    : in  std_logic_vector(31 downto 0);
    ack_app      : out std_logic;
    req_app      : in  std_logic_vector(31 downto 0)

    -- External Debug interface
--          write_enable_debug : out std_logic;
--          data_out_debug     : out std_logic_vector(31 downto 0);
--          busy_debug         : in  std_logic;

    );
end;

architecture HeMPS of HeMPS is          --Relacao entre as portas

  -- Interconnection signals 
  type txNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal tx             : txNPORT;
  type rxNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal rx             : rxNPORT;
  type clock_rxNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal clock_rx       : clock_rxNPORT;
  type clock_txNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal clock_tx       : clock_txNPORT;
  type credit_iNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal credit_i       : credit_iNPORT;
  type credit_oNport is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(3 downto 0);
  signal credit_o       : credit_oNPORT;
  type data_inNport is array (NUMBER_PROCESSORS - 1 downto 0) of arrayNPORT_1_regflit;
  signal data_in        : data_inNPORT;
  type data_outNport is array (NUMBER_PROCESSORS - 1 downto 0) of arrayNPORT_1_regflit;
  signal data_out       : data_outNPORT;
  signal address_router : std_logic_vector(7 downto 0);
  type router_position is array (NUMBER_PROCESSORS - 1 downto 0) of integer range 0 to TR;
  signal position       : router_position;

  type repo_address_t is array (NUMBER_PROCESSORS - 1 downto 0) of std_logic_vector(29 downto 0);
  signal repo_address_sig : repo_address_t;
  signal repo_data_sig    : arrayNPe_reg32;
  signal ack_app_sig      : regNPe;
  signal req_app_sig      : arrayNPe_reg32;

begin

  core_type_gen : for i in 0 to NUMBER_PROCESSORS-1 generate
    position(i) <= RouterPosition(i);
  end generate core_type_gen;


  proc : for i in 0 to NUMBER_PROCESSORS-1 generate  --Gera uma matriz de processadores homeogenios 

    PE : entity work.pe
      generic map (
        router_address => RouterAddress(i),
        kernel_type    => pe_type(i),
        log_file       => log_filename(i),
        manual_EAST    => ManualEASTByPos(i),
        manual_WEST    => ManualWESTByPos(i),
        manual_NORTH   => ManualNORTHByPos(i),
        manual_SOUTH   => ManualSOUTHByPos(i))
      port map(
        clock          => clock,
        reset          => reset,
        -- NoC
        clock_tx       => clock_tx(i),
        tx             => tx(i),
        data_out       => data_out(i),
        credit_i       => credit_i(i),
        clock_rx       => clock_rx(i),
        rx             => rx(i),
        data_in        => data_in(i),
        credit_o       => credit_o(i),
        -- External Memory
        repo_address   => repo_address_sig(i),
        repo_data_read => repo_data_sig(i),
        ack_app        => ack_app_sig(i),
        req_app        => req_app_sig(i)
        -- External Debug interface
--                        write_enable_debug  => write_enable_debug_sig(i),
--                        data_out_debug    => data_out_debug_sig(i),
--                        busy_debug      => busy_debug_sig(i)

        );

    ------------------------------------------------------------------------------
    --- REPOSITORY CONNECTIONS ----------------------------------------------------
    ------------------------------------------------------------------------------
    repo_mas : if pe_type(i) = "mas" generate  -- Caso o PE for mestre
      repo_address     <= repo_address_sig(i);
      repo_data_sig(i) <= repo_data;
      ack_app          <= ack_app_sig(i);
      req_app_sig(i)   <= req_app;
    end generate;

    ground_repo : if pe_type(i) /= "mas" generate  --Caso o PE nao for mestre
      repo_address_sig(i) <= (others => '0');
      repo_data_sig(i)    <= (others => '0');
      ack_app_sig(i)      <= '0';
      req_app_sig(i)      <= (others => '0');
    end generate;





    -- Returns the router position in the mesh
    -- BR: Botton Right
    -- BL: Botton Left
    -- TR: Top Right
    -- TL: Top Left 
    -- CRX: Center Right 
    -- CL: Center Left
    -- CC: Center
    -- 4x4 positions exemple
    --              TL TC TC TR
    --              CL CC CC CRX 
    --              CL CC CC CRX 
    --              BL BC BC BR



    ------------------------------------------------------------------------------
    --- EAST PORT CONNECTIONS ----------------------------------------------------
    ------------------------------------------------------------------------------
    east_border : if RouterPosition(i) = BR or RouterPosition(i) = CRX or RouterPosition(i) = TR generate
      data_injector : entity work.inject_data
        generic map(router_nb => i,
                    port_name => "EAST")
        port map (clock    => clock,
                  reset    => reset,
                  clock_rx => clock_rx(i)(EAST),
                  rx       => rx(i)(EAST),
                  data_in  => data_in(i)(EAST),
                  credit_o => credit_o(i)(EAST));

      data_consumer : entity work.receive_data
        generic map(router_nb => i,
                    port_name => "EAST")
        port map(clock    => clock,
                 reset    => reset,
                 clock_tx => clock_tx(i)(EAST),
                 tx       => tx(i)(EAST),
                 data_out => data_out(i)(EAST),
                 credit_i => credit_i(i)(EAST));
    end generate;

    east_connection : if RouterPosition(i) = BL or RouterPosition(i) = CL or RouterPosition(i) = TL or RouterPosition(i) = BC or RouterPosition(i) = TC or RouterPosition(i) = CC generate
      rx(i)(EAST)       <= tx(i+1)(WEST);
      clock_rx(i)(EAST) <= clock_tx(i+1)(WEST);
      credit_i(i)(EAST) <= credit_o(i+1)(WEST);
      data_in(i)(EAST)  <= data_out(i+1)(WEST);
    end generate;

    ------------------------------------------------------------------------------
    --- WEST PORT CONNECTIONS ----------------------------------------------------
    ------------------------------------------------------------------------------
    west_border : if RouterPosition(i) = BL or RouterPosition(i) = CL or RouterPosition(i) = TL generate
      data_injector : entity work.inject_data
        generic map(router_nb => i,
                    port_name => "WEST")
        port map (clock    => clock,
                  reset    => reset,
                  clock_rx => clock_rx(i)(WEST),
                  rx       => rx(i)(WEST),
                  data_in  => data_in(i)(WEST),
                  credit_o => credit_o(i)(WEST));

      data_consumer : entity work.receive_data
        generic map(router_nb => i,
                    port_name => "WEST")
        port map(clock    => clock,
                 reset    => reset,
                 clock_tx => clock_tx(i)(WEST),
                 tx       => tx(i)(WEST),
                 data_out => data_out(i)(WEST),
                 credit_i => credit_i(i)(WEST));
    end generate;

    west_connection : if (RouterPosition(i) = BR or RouterPosition(i) = CRX or RouterPosition(i) = TR or RouterPosition(i) = BC or RouterPosition(i) = TC or RouterPosition(i) = CC) generate
      rx(i)(WEST)       <= tx(i-1)(EAST);
      clock_rx(i)(WEST) <= clock_tx(i-1)(EAST);
      credit_i(i)(WEST) <= credit_o(i-1)(EAST);
      data_in(i)(WEST)  <= data_out(i-1)(EAST);
    end generate;

    -------------------------------------------------------------------------------
    --- NORTH PORT CONNECTIONS ----------------------------------------------------
    -------------------------------------------------------------------------------
    north_border : if RouterPosition(i) = TL or RouterPosition(i) = TC or RouterPosition(i) = TR generate
      data_injector : entity work.inject_data
        generic map(router_nb => i,
                    port_name => "NORTH")
        port map (clock    => clock,
                  reset    => reset,
                  clock_rx => clock_rx(i)(NORTH),
                  rx       => rx(i)(NORTH),
                  data_in  => data_in(i)(NORTH),
                  credit_o => credit_o(i)(NORTH));

      data_consumer : entity work.receive_data
        generic map(router_nb => i,
                    port_name => "NORTH")
        port map(clock    => clock,
                 reset    => reset,
                 clock_tx => clock_tx(i)(NORTH),
                 tx       => tx(i)(NORTH),
                 data_out => data_out(i)(NORTH),
                 credit_i => credit_i(i)(NORTH));
    end generate;

    north_connection : if RouterPosition(i) = BL or RouterPosition(i) = BC or RouterPosition(i) = BR or RouterPosition(i) = CL or RouterPosition(i) = CRX or RouterPosition(i) = CC generate
      rx(i)(NORTH)       <= tx(i+NUMBER_PROCESSORS_X)(SOUTH);
      clock_rx(i)(NORTH) <= clock_tx(i+NUMBER_PROCESSORS_X)(SOUTH);
      credit_i(i)(NORTH) <= credit_o(i+NUMBER_PROCESSORS_X)(SOUTH);
      data_in(i)(NORTH)  <= data_out(i+NUMBER_PROCESSORS_X)(SOUTH);
    end generate;

    --------------------------------------------------------------------------------
    --- SOUTH PORT CONNECTIONS -----------------------------------------------------
    ---------------------------------------------------------------------------
    south_border : if RouterPosition(i) = BL or RouterPosition(i) = BC or RouterPosition(i) = BR generate
      data_injector : entity work.inject_data
        generic map(router_nb => i,
                    port_name => "SOUTH")
        port map (clock    => clock,
                  reset    => reset,
                  clock_rx => clock_rx(i)(SOUTH),
                  rx       => rx(i)(SOUTH),
                  data_in  => data_in(i)(SOUTH),
                  credit_o => credit_o(i)(SOUTH));

      data_consumer : entity work.receive_data
        generic map(router_nb => i,
                    port_name => "SOUTH")
        port map(clock    => clock,
                 reset    => reset,
                 clock_tx => clock_tx(i)(SOUTH),
                 tx       => tx(i)(SOUTH),
                 data_out => data_out(i)(SOUTH),
                 credit_i => credit_i(i)(SOUTH));
    end generate;

    south_connection : if RouterPosition(i) = TL or RouterPosition(i) = TC or RouterPosition(i) = TR or RouterPosition(i) = CL or RouterPosition(i) = CRX or RouterPosition(i) = CC generate
      rx(i)(SOUTH)       <= tx(i-NUMBER_PROCESSORS_X)(NORTH);
      clock_rx(i)(SOUTH) <= clock_tx(i-NUMBER_PROCESSORS_X)(NORTH);
      credit_i(i)(SOUTH) <= credit_o(i-NUMBER_PROCESSORS_X)(NORTH);
      data_in(i)(SOUTH)  <= data_out(i-NUMBER_PROCESSORS_X)(NORTH);
    end generate;
  end generate proc;

end architecture;
