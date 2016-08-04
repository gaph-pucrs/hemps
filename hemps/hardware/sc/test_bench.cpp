//------------------------------------------------------------------------------------------------
//
//  DISTRIBUTED HEMPS -  5.0
//
//  Research group: GAPH-PUCRS    -    contact   fernando.moraes@pucrs.br
//
//  Distribution:  September 2013
//
//  Source name:  test_bench.cpp
//
//  Brief description: Testbench debugger
//
//------------------------------------------------------------------------------------------------

#include "test_bench.h"

#ifdef MTI_SYSTEMC
SC_MODULE_EXPORT(test_bench);
#endif

void test_bench::load_repository(){
	string line;
	int i = 0;
	ifstream repo_file ("repository.txt");

	if (repo_file.is_open()) {
		while ( getline (repo_file,line) ) {

			if (i == REPO_SIZE){
				cout << "ERROR: Repository file repository.txt is greater than REPOSIZE = " << REPO_SIZE << endl;
				sc_stop();
			}

			//Converts a hex string to unsigned integer
			sscanf( line.substr(0, 8).c_str(), "%lx", &repository[i] );
			i++;

		}
		repo_file.close();
	} else {
		cout << "Unable to open file repository.txt" << endl;
	}
}

void test_bench::load_appstart(){
	string line;
	int i = 0;
	ifstream appstart_file ("appstart.txt");

	if (appstart_file.is_open()) {
		while ( getline (appstart_file,line) ) {

			if (i == APPSTART_SIZE){
				cout << "ERROR: App Start file appstart.txt is greater than APPSTART_SIZE = " << APPSTART_SIZE << "\nPlease, recompile apps and hw" <<endl;
				sc_stop();
			}

			//Converts a hex string to unsigned integer
			sscanf( line.substr(0, 8).c_str(), "%lx", &appstart[i] );
			i++;

		}
		appstart_file.close();
	} else {
		cout << "Unable to open file appstart.txt" << endl;
	}
}

void test_bench::read_repository(){

	unsigned int index = (unsigned int)address[0].read()(25,0);

	index = index / 4;

	if (index < REPO_SIZE){
		data_read[0].write(repository[index]);
	}
}

void test_bench::new_app(){
	
	unsigned int app_repo_address = 0;
	unsigned int app_start_time_ms = 0;

	if (reset.read() == 1)  {

		req_app[0].write(0);
		app_i = 0;
		current_time = 0;

	} else if (clock.read() == 1){

		if (req_app[0].read() == 0){

			app_repo_address = appstart[app_i];

			if (app_repo_address != 0xdeadc0de){

				app_start_time_ms = appstart[app_i+1];

				if ( (app_start_time_ms * 100000) <= current_time ){

					req_app[0].write(0x80000000 | app_repo_address);

					cout << "Repository requesting app " << (app_i/2) << " at address " << hex << app_repo_address << endl;

					app_i = app_i + 2;
				}
			}

		} else if (ack_app[0].read() == 1){
			req_app[0].write(0);
			cout << "Ack received!" << endl;
		}

		current_time++;
	}
}

void test_bench::ClockGenerator(){
	while(1){
		clock.write(0);
		wait (5, SC_NS);					//Allow signals to set
		clock.write(1);
		wait (5, SC_NS);					//Allow signals to set
	}
}
	
void test_bench::resetGenerator(){
	reset.write(1);
	wait (70, SC_NS);
	reset.write(0);
}

	
