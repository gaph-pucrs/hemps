//------------------------------------------------------------------------------------------------
//
//  HEMPS -  7.0
//
//  Research group: GAPH-PUCRS    -    contact   fernando.moraes@pucrs.br
//
//  Distribution:  2016
//
//  Source name:  hemps.h
//
//  Brief description: to do
//
//------------------------------------------------------------------------------------------------

#include <systemc.h>
#include "standards.h"
#include "pe/pe.h"

#define BL 0
#define BC 1
#define BR 2
#define CL 3
#define CC 4
#define CRX 5
#define TL 6
#define TC 7
#define TR 8



SC_MODULE(hemps) {
	
	sc_in< bool >			clock;
	sc_in< bool >			reset;

	//Tasks repository interface
	sc_out<sc_uint<30> >	mem_addr[N_PE];
	sc_in<sc_uint<32> >		data_read[N_PE];
	
	//Dynamic Insertion of Applications
	sc_out<bool >			ack_app[N_PE];
	sc_in<sc_uint<32> >		req_app[N_PE];
	
	// NoC Interface
	sc_signal<bool >		clock_tx[N_PE][NPORT-1];
	sc_signal<bool >		tx[N_PE][NPORT-1];
	sc_signal<regflit >		data_out[N_PE][NPORT-1];
	sc_signal<bool >		credit_i[N_PE][NPORT-1];
	
	sc_signal<bool >		clock_rx[N_PE][NPORT-1];
	sc_signal<bool > 		rx[N_PE][NPORT-1];
	sc_signal<regflit >		data_in[N_PE][NPORT-1];
	sc_signal<bool >		credit_o[N_PE][NPORT-1];
		
	pe  *	PE[N_PE];//store slaves PEs
	
	int i,j;
	
	int RouterPosition(int router);
	regaddress RouterAddress(int router);
	regaddress r_addr;
 	void pes_interconnection();
 	
	char pe_name[20];
	int x_addr, y_addr;
	SC_CTOR(hemps){
		
		for (j = 0; j < N_PE; j++) {

			r_addr = RouterAddress(j);
			x_addr = ((int) r_addr) >> 8;
			y_addr = ((int) r_addr) & 0xFF;

			sprintf(pe_name, "PE%dx%d", x_addr, y_addr);
			printf("Creating PE %s\n", pe_name);

			PE[j] = new pe(pe_name, r_addr);
			PE[j]->clock(clock);
			PE[j]->reset(reset);
			PE[j]->address(mem_addr[j]);
			PE[j]->data_read(data_read[j]);
			PE[j]->ack_app(ack_app[j]);
			PE[j]->req_app(req_app[j]);

			for (i = 0; i < NPORT - 1; i++) {
				PE[j]->clock_tx[i](clock_tx[j][i]);
				PE[j]->tx[i](tx[j][i]);
				PE[j]->data_out[i](data_out[j][i]);
				PE[j]->credit_i[i](credit_i[j][i]);
				PE[j]->clock_rx[i](clock_rx[j][i]);
				PE[j]->data_in[i](data_in[j][i]);
				PE[j]->rx[i](rx[j][i]);
				PE[j]->credit_o[i](credit_o[j][i]);
			}
		}

		SC_METHOD(pes_interconnection);
		for (j = 0; j < N_PE; j++) {
			for (i = 0; i < NPORT - 1; i++) {
				sensitive << clock_tx[j][i];
				sensitive << tx[j][i];
				sensitive << data_out[j][i];
				sensitive << credit_i[j][i];
				sensitive << clock_rx[j][i];
				sensitive << data_in[j][i];
				sensitive << rx[j][i];
				sensitive << credit_o[j][i];
			}
		}
	}
};

