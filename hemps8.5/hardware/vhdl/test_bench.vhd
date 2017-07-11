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

--! @file
--! @ingroup vhdl_group
--! @{
--! @}

--! @brief entity brief description
 
--! @detailed detailed description
entity test_bench is
        
        --constant	log_file            : string := "output_master.txt"; --! port description
        constant	mlite_description   : string := "RTL";
     	constant	ram_description     : string := "RTL";
     	constant	router_description  : string := "RTL";
        
        constant	REPO_SIZE: integer := (TOTAL_REPO_SIZE_BYTES/4); --This math is because each repoline is 32 bits word
   		constant	APPSTART_SIZE: integer := (APP_NUMBER*2)+1; --THis math is because the appstart file have two values per app plus one end of file mark 
        type repo_type is array(REPO_SIZE-1 downto 0) of std_logic_vector(31 downto 0);
        type appstart_type is array(APPSTART_SIZE-1 downto 0) of std_logic_vector(31 downto 0);
        
        
        impure function load_repo (repo_file : in string) return repo_type is
	        
	        file file_ptr		: text open read_mode is repo_file;
  			variable inline    : line;
  			variable i         : integer := 0;
  			
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

    	
    	impure function load_appstart (appstart_file : in string) return appstart_type is
	        
	        file file_ptr2		: text open read_mode is appstart_file;
  			variable inline    : line;
  			variable i         : integer := 0;
  			
  			variable mem : appstart_type := (others => (others => '0'));
	        
  		begin
  			
  			while not endfile(file_ptr2) loop 
  				
  				if (i = REPO_SIZE) then
	  		    	assert false report "ERROR: appstart access overflow - i= " & integer'image(i)
	  		    	severity error;
	  		    end if;
	  		    
	  		    readline(file_ptr2, inline);
  				hread(inline, mem(i));
  				i := i + 1;
  				
  			end loop;
			
			file_close(file_ptr2);
			
        	return mem;
        	
    	end load_appstart; 
    	
        
end;

architecture test_bench of test_bench is
	
	
        signal clock                      : std_logic := '0';
        signal clock_200                  : std_logic := '1';
        signal reset                      : std_logic;
--        signal control_write_enable_debug : std_logic;
--        signal control_data_out_debug     : std_logic_vector(31 downto 0);
--        signal control_busy_debug         : std_logic;
        signal control_hemps_addr         : std_logic_vector(29 downto 0);
        signal control_hemps_data         : std_logic_vector(31 downto 0);
        
        type state is (LER, WAIT_DDR, WR_HEMPS, START);
        signal EA : state;
        type state2 is (S0, S1);
        
        signal CS           : state2;
        signal counter      : integer       := 0;
        signal ack_app      : std_logic;
        signal req_app      : std_logic_vector(31 downto 0);
        
        constant repository : repo_type     := load_repo("repository.txt");
        constant appstart   : appstart_type := load_appstart("appstart.txt");
        
        
		signal current_time      : integer := 0;
		signal app_i             : integer := 0;
		
begin

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
		clock => clock,
		reset => reset,
		--repository
		repo_address => control_hemps_addr,
		repo_data => control_hemps_data,
		ack_app => ack_app,
		req_app => req_app
	--debug
	--		write_enable_debug => control_write_enable_debug,
	--		data_out_debug     => control_data_out_debug,
	--		busy_debug         => control_busy_debug,
			
	);
	   
	   
	   
	reset     <= '1', '0' after 100 ns;
	-- 100 MHz
	clock     <= not clock after 5 ns;
	-- 200 MHz
	clock_200 <= not clock_200 after 1.25 ns;
	
	--Repository data assignment
	control_hemps_data <= repository(CONV_INTEGER(control_hemps_addr(23 downto 0)) / 4);
       
-- App request control

	process (clock, reset)
		variable app_repo_address  : integer := 0;
		variable app_start_time_ms : integer := 0;
    begin
    	
    	if reset = '1' then
    		
    		req_app <= (others => '0');
    		app_i <= 0;
    		current_time <= 0;
    	
    	elsif rising_edge(clock) then
    		
    		current_time <= current_time + 1;
    		
    		if req_app = x"00000000" then
    	
	    		if appstart(app_i) /= x"deadc0de" then
	    		
		    		app_repo_address := conv_integer(appstart(app_i));
		    		app_start_time_ms := conv_integer(appstart(app_i+1));
		    		
		    		if (app_start_time_ms * 100000) <= current_time then
		    			
		    			req_app <= CONV_STD_LOGIC_VECTOR(app_repo_address, 32) or x"80000000"; 
		    			
		    			assert false report "Repository requesting app "& integer'image(app_i/2)
	  		    		severity note;

		    			app_i <= app_i+2;
		    			
		    		end if;
	    		end if;
	    	elsif ack_app = '1' then
	    		req_app <= (others => '0');
	    	end if;
    			
    	end if;
    		
    end process;
        
     --
     -- creates the output file. This code was not removed because it can be useful in a protipation. In fact, all debug traces all only commented in testbench, hemps, pe
     --
--     process(control_write_enable_debug,reset)
--       file store_file : text open write_mode is log_file;
--       variable file_line : line;
--       variable line_type: character;
--       variable line_length : natural := 0;
--       variable str: string (1 to 4);
--       variable str_end: boolean;
--     begin
--        if reset = '1' then
--                str_end := false;
--                CS <= S0;      
--        elsif rising_edge(control_write_enable_debug) then
--                case CS is
--                  when S0 =>
--                          -- Reads the incoming string
--                          line_type := character'val(conv_integer(control_data_out_debug(7 downto 0)));
--                          
--                          -- Verifies if the string is from Echo()
--                          if line_type = '$' then 
--                                  write(file_line, line_type);
--                                  line_length := line_length + 1;
--                                  CS <= S1;
--                          
--                          -- Writes the string to the file
--                          else                                                                    
--                                  str(4) := character'val(conv_integer(control_data_out_debug(7 downto 0)));
--                                  str(3) := character'val(conv_integer(control_data_out_debug(15 downto 8)));
--                                  str(2) := character'val(conv_integer(control_data_out_debug(23 downto 16)));
--                                  str(1) := character'val(conv_integer(control_data_out_debug(31 downto 24)));
--                                  
--                                  str_end := false;
--                                  
--                                  for i in 1 to 4 loop                                                            
--                                          -- Writes a string in the line
--                                          if str(i) /= lf and str(i) /= nul and not str_end then
--                                                  write(file_line, str(i));
--                                                  line_length := line_length + 1;
--                                  
--                                          -- Detects the string end
--                                          elsif str(i) = nul then
--                                                  str_end := true;
--                                          
--                                          -- Line feed detected. Writes the line in the file
--                                          elsif str(i) = lf then                                                              
--                                                  writeline(store_file, file_line);
--                                                  line_length := 0;
--                                          end if;
--                                  end loop;
--                          end if;
--                                                                  
--                  -- Receives from plasma the source processor, source task and writes them to the file
--                  when S1 =>
--                          write(file_line, ',');
--                          write(file_line, conv_integer(control_data_out_debug(7 downto 0)));                                                             
--                          line_length := line_length + 1;
--                          
--                          if line_length = 3 then 
--                                  write(file_line, ',');
--                                  CS <= S0;
--                          else
--                                  CS <= S1;
--                          end if;
--               end case;
--        end if;
--      end process;

end test_bench;