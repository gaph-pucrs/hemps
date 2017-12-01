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
    req_app      : in  std_logic_vector(31 downto 0);

    --NoC Interface (IO)
    tx_io       : out std_logic_vector((IO_NUMBER-1) downto 0);
    data_out_io : out arrayNio_regflit;
    credit_i_io : in  std_logic_vector((IO_NUMBER-1) downto 0);
    clock_tx_io : out std_logic_vector((IO_NUMBER-1) downto 0);
    rx_io       : in  std_logic_vector((IO_NUMBER-1) downto 0);
    data_in_io  : in  arrayNio_regflit;
    credit_o_io : out std_logic_vector((IO_NUMBER-1) downto 0);
    clock_rx_io : in  std_logic_vector((IO_NUMBER-1) downto 0)

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
        kernel_type    => "sla",
        log_file       => log_filename(i),
        simple_soc     => SIMPLE_SOC,
        manual_NORTH   => ManualNORTHbyPos(i),
        manual_SOUTH   => ManualSOUTHbyPos(i),
        manual_EAST    => ManualEASTbyPos(i),
        manual_WEST    => ManualWESTbyPos(i))
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
    repo_mas : if RouterPosition(i) = BL generate
      repo_address     <= repo_address_sig(i);
      repo_data_sig(i) <= repo_data;
      ack_app          <= ack_app_sig(i);
      req_app_sig(i)   <= req_app;
    end generate;

    ground_repo : if RouterPosition(i) /= BL generate
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
    east_io : if OPEN_IO(i) = "eas" generate
      rx(i)(EAST)              <= rx_io(io_index(i));
      clock_rx(i)(EAST)        <= clock_rx_io(io_index(i));
      credit_i(i)(EAST)        <= credit_i_io(io_index(i));
      data_in(i)(EAST)         <= data_in_io(io_index(i));
      tx_io(io_index(i))       <= tx(i)(EAST);
      clock_tx_io(io_index(i)) <= clock_tx(i)(EAST);
      credit_o_io(io_index(i)) <= credit_o(i)(EAST);
      data_out_io(io_index(i)) <= data_out(i)(EAST);
    end generate;
    east_grounding : if OPEN_IO(i) = "gnd" and (RouterPosition(i) = BR or RouterPosition(i) = CRX or RouterPosition(i) = TR) generate
      rx(i)(EAST)       <= '0';
      clock_rx(i)(EAST) <= '0';
      credit_i(i)(EAST) <= '0';
      data_in(i)(EAST)  <= (others => '0');
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
    west_io : if OPEN_IO(i) = "wes" generate
      rx(i)(WEST)              <= rx_io(io_index(i));
      clock_rx(i)(WEST)        <= clock_rx_io(io_index(i));
      credit_i(i)(WEST)        <= credit_i_io(io_index(i));
      data_in(i)(WEST)         <= data_in_io(io_index(i));
      tx_io(io_index(i))       <= tx(i)(WEST);
      clock_tx_io(io_index(i)) <= clock_tx(i)(WEST);
      credit_o_io(io_index(i)) <= credit_o(i)(WEST);
      data_out_io(io_index(i)) <= data_out(i)(WEST);
    end generate;
    west_grounding : if OPEN_IO(i) = "gnd" and (RouterPosition(i) = BL or RouterPosition(i) = CL or RouterPosition(i) = TL) generate
      rx(i)(WEST)       <= '0';
      clock_rx(i)(WEST) <= '0';
      credit_i(i)(WEST) <= '0';
      data_in(i)(WEST)  <= (others => '0');
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
    north_io : if OPEN_IO(i) = "nor" generate
      rx(i)(NORTH)             <= rx_io(io_index(i));
      clock_rx(i)(NORTH)       <= clock_rx_io(io_index(i));
      credit_i(i)(NORTH)       <= credit_i_io(io_index(i));
      data_in(i)(NORTH)        <= data_in_io(io_index(i));
      tx_io(io_index(i))       <= tx(i)(NORTH);
      clock_tx_io(io_index(i)) <= clock_tx(i)(NORTH);
      credit_o_io(io_index(i)) <= credit_o(i)(NORTH);
      data_out_io(io_index(i)) <= data_out(i)(NORTH);
    end generate;
    north_grounding : if OPEN_IO(i) = "gnd" and (RouterPosition(i) = TL or RouterPosition(i) = TC or RouterPosition(i) = TR) generate
      rx(i)(NORTH)       <= '0';
      clock_rx(i)(NORTH) <= '0';
      credit_i(i)(NORTH) <= '0';
      data_in(i)(NORTH)  <= (others => '0');
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
    south_io : if OPEN_IO(i) = "sou" generate
      rx(i)(SOUTH)             <= rx_io(io_index(i));
      clock_rx(i)(SOUTH)       <= clock_rx_io(io_index(i));
      credit_i(i)(SOUTH)       <= credit_i_io(io_index(i));
      data_in(i)(SOUTH)        <= data_in_io(io_index(i));
      tx_io(io_index(i))       <= tx(i)(SOUTH);
      clock_tx_io(io_index(i)) <= clock_tx(i)(SOUTH);
      credit_o_io(io_index(i)) <= credit_o(i)(SOUTH);
      data_out_io(io_index(i)) <= data_out(i)(SOUTH);
    end generate;
    south_grounding : if OPEN_IO(i) = "gnd" and (RouterPosition(i) = BL or RouterPosition(i) = BC or RouterPosition(i) = BR) generate
      rx(i)(SOUTH)       <= '0';
      clock_rx(i)(SOUTH) <= '0';
      credit_i(i)(SOUTH) <= '0';
      data_in(i)(SOUTH)  <= (others => '0');
    end generate;

    south_connection : if RouterPosition(i) = TL or RouterPosition(i) = TC or RouterPosition(i) = TR or RouterPosition(i) = CL or RouterPosition(i) = CRX or RouterPosition(i) = CC generate
      rx(i)(SOUTH)       <= tx(i-NUMBER_PROCESSORS_X)(NORTH);
      clock_rx(i)(SOUTH) <= clock_tx(i-NUMBER_PROCESSORS_X)(NORTH);
      credit_i(i)(SOUTH) <= credit_o(i-NUMBER_PROCESSORS_X)(NORTH);
      data_in(i)(SOUTH)  <= data_out(i-NUMBER_PROCESSORS_X)(NORTH);
    end generate;
  end generate proc;

end architecture;
