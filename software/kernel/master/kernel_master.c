/*!\file kernel_master.c
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief
 * Kernel master is the system manager kernel.
 *
 * \detailed
 * kernel_master is the core of the OS running into the managers processors (local and global).
 * It assumes two operation modes: global or local. The operation modes is defined by the global variable is_global_master.
 * Local operation mode: runs into local managers. Manage the applications and task mapping.
 * Global operation mode: runs into global manager. Runs all functions of the local operation mode, further the applications admission control.
 * The kernel_master file uses several modules that implement specific functions
 */

#include "kernel_master.h"

#include "../../modules/utils.h"
#include "../../include/plasma.h"
#include "../../include/services.h"
#include "../../modules/packet.h"
#include "../../modules/new_task.h"
#include "../../modules/resource_manager.h"
#include "../../modules/reclustering.h"
#include "../../modules/applications.h"
#include "../../modules/processors.h"


/*Local Manager (LM) global variables*/
unsigned int 	pending_app_to_map = 0; 					//!< Controls the number of pending applications already handled by not completely mapped
unsigned char 	is_global_master;							//!< Defines if this kernel is at global or local operation mode
unsigned int 	global_master_address;						//!< Used to stores the global master address, is useful in local operation mode
unsigned int 	terminated_task_master[MAX_TASKS_APP];		//!< Auxiliary array that stores the terminated task list


/*Global Master (GM) global variables*/
unsigned int 	total_mpsoc_resources = (MAX_LOCAL_TASKS * MAX_SLAVE_PROCESSORS);	//!< Controls the number of total slave processors pages available. Is the admission control variable
unsigned int 	cluster_load[CLUSTER_NUMBER];										//!< Keep the cluster load, updated at every applications start and finish
unsigned int 	terminated_app_count = 0;											//!< Used to fires the END OF ALL APPLIATIONS
unsigned int 	waiting_app_allocation = 0;											//!< Signal that an application is not fully mapped


/** Receive a address and return the cluster index of array cluster_info[]
 *  \param x Address x of the manager processor
 *  \param y Address y of the manager processor
 *  \return Index of array cluster_info[]
 */
int get_cluster_ID(int x, int y){

	for (int i=0; i<CLUSTER_NUMBER; i++){
		if (cluster_info[i].master_x == x && cluster_info[i].master_y == y){
			return i;
		}
	}
	puts("ERROR - cluster nao encontrado\n");
	return -1;
}

/** Receives a slave address and tells which is its master address
 *  \param slave_address The XY slave address
 *  \return The master address of the slave. Return -1 if there is no master (ERROR situation)
 */
int get_master_address(int slave_address){

	ClusterInfo *cf;
	int proc_x, proc_y;

	proc_x = slave_address >> 8;
	proc_y = slave_address & 0xFF;

	for(int i=0; i<CLUSTER_NUMBER; i++){
		cf = &cluster_info[i];
		if (cf->xi <= proc_x && cf->xf >= proc_x && cf->yi <= proc_y && cf->yf >= proc_y){
			return get_cluster_proc(i);
		}
	}

	puts("ERROR: no master address found\n");
	while(1);
	return -1;
}

/** Assembles and sends a APP_TERMINATED packet to the global master
 *  \param app The Applications address
 *  \param terminated_task_list The terminated task list of the application
 */
void send_app_terminated(Application *app, unsigned int * terminated_task_list){

	ServiceHeader *p = get_service_header_slot();

	p->header = global_master_address;

	p->service = APP_TERMINATED;

	p->app_ID = app->app_ID;

	p->app_task_number = app->tasks_number;

	send_packet(p, (unsigned int) terminated_task_list, app->tasks_number);

	while (MemoryRead(DMNI_SEND_ACTIVE));

}

/** Assembles and sends a TASK_ALLOCATION packet to a slave kernel
 *  \param new_t The NewTask instance
 */
void send_task_allocation(NewTask * new_t){

	ServiceHeader *p = get_service_header_slot();

	p->header = new_t->allocated_processor;

	p->service = TASK_ALLOCATION;

	p->master_ID = new_t->master_ID;

	p->task_ID = new_t->task_ID;

	p->code_size = new_t->code_size;

	send_packet(p, (0x10000000 | new_t->initial_address), new_t->code_size);

	puts("Task allocation send - id: "); puts(itoa(p->task_ID)); puts(" send to proc: "); puts(itoh(p->header)); puts("\n");

}

/** Assembles and sends a TASK_RELEASE packet to a slave kernel
 *  \param app The Application instance
 */
void send_task_release(Application * app){

	ServiceHeader *p;
	unsigned int app_tasks_location[app->tasks_number];

	for (int i =0; i<app->tasks_number; i++){
		app_tasks_location[i] = app->tasks[i].allocated_proc;
	}

	for (int i =0; i<app->tasks_number; i++){

		p = get_service_header_slot();

		p->header = app->tasks[i].allocated_proc;

		p->service = TASK_RELEASE;

		p->app_task_number = app->tasks_number;

		p->task_ID = app->tasks[i].id;

		p->data_size = app->tasks[i].data_size;

		p->bss_size = app->tasks[i].bss_size;

		send_packet(p, (unsigned int) app_tasks_location, app->tasks_number);

		app->tasks[i].status = TASK_RUNNING;

		putsv("\n -> send TASK_RELEASE to task ", p->task_ID);
		//puts(" in proc "); puts(itoh(p->header)); puts("\n----\n");
	}

	app->status = RUNNING;

	while(MemoryRead(DMNI_SEND_ACTIVE));
}

/** Assembles and sends a APP_ALLOCATION_REQUEST packet to the global master
 *  \param app The Application instance
 *  \param task_info An array containing relevant task informations
 */
void send_app_allocation_request(Application * app, unsigned int * task_info){

	ServiceHeader *p;

	p = get_service_header_slot();

	p->header = global_master_address;

	p->service = APP_ALLOCATION_REQUEST;

	p->master_ID = net_address;

	p->app_task_number = app->tasks_number;

	putsv("Send new APP REQUEST to master - id: ", p->task_ID );
	putsv(" app id: ", app->app_ID);

	send_packet(p, (unsigned int) task_info, app->tasks_number*4);

	while (MemoryRead(DMNI_SEND_ACTIVE));

}

/** Assembles and sends a TASK_MIGRATION packet to a slave kernel
 *  \param task_ID The task ID to be migrated
 *  \param new_proc The new processor address of task_ID
 */
int send_task_migration(int task_ID, int new_proc){


	int old_proc;
	Application * app;

	app = get_application_ptr(task_ID >> 8);

	if (app->status != RUNNING){
		putsvsv("Warning! :: Task migration for task ", task_ID, " refused, is not running app ", (task_ID >> 8));
		return 0;
	}

	ServiceHeader *p = get_service_header_slot();

	old_proc = get_task_location(task_ID);

	p->header = old_proc;

	p->service = TASK_MIGRATION;

	p->task_ID = task_ID;

	p->allocated_processor = new_proc;

	send_packet(p, 0, 0);

	//Update task status
	set_task_migrating(task_ID);

	putsvsv("Task migration order of task ", task_ID, " to proc ", old_proc);

	return 1;

}

/** Requests a new application to the global master kernel
 *  \param app Application to be requested
 */
void request_application(Application *app){

	Task *t;
	NewTask nt;
	unsigned int task_info[app->tasks_number*4];
	int index_counter;

	//puts("\nRequest APP\n");

	pending_app_to_map--;

	index_counter = 0;

	for (int i=0; i<app->tasks_number; i++){

		t = &app->tasks[i];

		if (t->allocated_proc == -1){
			putsv("ERROR task id not allocated: ", t->id);
			while(1);
		}

		t->status = REQUESTED;

		if (is_global_master){

			//Is equivalent to the APP ALLOCATION_REQUEST TREATMENT
			nt.allocated_processor = t->allocated_proc;
			nt.initial_address = t->initial_address;
			nt.code_size = t->code_size;
			nt.master_ID = net_address;
			nt.task_ID = t->id;

			add_new_task(&nt);

		} else {

			task_info[index_counter++] = t->id;
			task_info[index_counter++] = t->allocated_proc;
			task_info[index_counter++] = t->initial_address;
			task_info[index_counter++] = t->code_size;
		}
	}

	if (is_global_master){

		waiting_app_allocation = 0;

	} else {

		send_app_allocation_request(app, task_info);
	}

}

/** Handles a pending application. A pending application it the one which there is some task to be inserted into reclustering
 */
void handle_pending_application(){

	Application *app = 0;
	int request_app = 0;

	puts("Handle next application \n");

	/*Selects an application pending to be mapped due reclustering*/
	app = get_next_pending_app();

	//This line fires the reclustering protocol
	request_app = reclustering_next_task(app);

	if (request_app){

		app->status = READY_TO_LOAD;

		request_application(app);
	}
}

/** Handles an application which terminated its execution
 *  \param appID Application ID of the terminated app
 *  \param app_task_number Application task number
 *  \param app_master_addr Application master XY address
 */
void handle_app_terminated(int appID, unsigned int app_task_number, unsigned int app_master_addr){

	unsigned int task_master_addr;
	int borrowed_cluster, original_cluster;

	//putsv("\n --- > Handle APP terminated- app ID: ", appID);
	//puts("original master addrr "); puts(itoh(app_master_addr)); puts("\n");

	original_cluster = get_cluster_ID(app_master_addr >> 8, app_master_addr & 0xFF);

	for (int i=0; i<app_task_number; i++){
		task_master_addr = terminated_task_master[i];

		//puts("Terminated task id "); puts(itoa(appID << 8 | i)); puts(" with physycal master addr "); puts(itoh(task_master_addr)); puts("\n");

		if (task_master_addr != app_master_addr && task_master_addr != net_address){

			borrowed_cluster = get_cluster_ID(task_master_addr >> 8, task_master_addr & 0xFF);

			release_cluster_resources(borrowed_cluster, 1);

		} else if (task_master_addr != net_address){ // Because only the global calls this funtion
			//puts("Remove original\n");
			release_cluster_resources(original_cluster, 1);
		}
	}
	total_mpsoc_resources += app_task_number;
	terminated_app_count++;
	puts("SIMUL_PROGRESS "); puts(itoa(((terminated_app_count*100)/APP_NUMBER)));puts("%\n");

	//puts("\n-------\n");

	if (terminated_app_count == APP_NUMBER){
		puts("FINISH ");puts(itoa(MemoryRead(TICK_COUNTER))); puts("\n");
		MemoryWrite(END_SIM,1);
	}

}

/** Handles a new packet from NoC
 */
void handle_packet() {

	int app_id, allocated_tasks, index_counter, master_addr;
	volatile ServiceHeader p;
	NewTask nt;
	Application *app;
	unsigned int task_info[MAX_TASKS_APP*4];

	read_packet((ServiceHeader *)&p);

	switch (p.service){

	case NEW_APP:

		handle_new_app(p.app_ID, 0, p.app_descriptor_size);

		break;

	case TASK_ALLOCATED:

		putsv("\n -> TASK ALLOCATED from task ", p.task_ID);
		app_id = p.task_ID >> 8;

		app = get_application_ptr(app_id);

		allocated_tasks = set_task_allocated(app, p.task_ID);

		putsv("Allocated tasks: ", allocated_tasks);

		if (allocated_tasks == app->tasks_number){

			send_task_release(app);
		}

		break;

	case INITIALIZE_CLUSTER:

		global_master_address = p.source_PE;

		reclustering_setup(p.cluster_ID);

		initialize_slaves();

		break;

	case LOAN_PROCESSOR_REQUEST:
	case LOAN_PROCESSOR_DELIVERY:
	case LOAN_PROCESSOR_RELEASE:

		handle_reclustering((ServiceHeader *)&p);

		break;

	case APP_ALLOCATION_REQUEST:

		//puts("New app allocation request\n");

		index_counter = 0;

		DMNI_read_data((unsigned int)task_info, p.app_task_number*4);

		for(int i=0; i< p.app_task_number; i++){

			nt.task_ID = task_info[index_counter++];
			nt.allocated_processor = task_info[index_counter++];
			nt.initial_address = task_info[index_counter++];
			nt.code_size = task_info[index_counter++];
			nt.master_ID = p.master_ID;

			add_new_task(&nt);

			puts("New task requisition: "); puts(itoa(nt.task_ID)); puts(" allocated proc ");
			puts(itoh(nt.allocated_processor)); puts("\n");

			/*These lines above mantain the cluster resource control at a global master perspective*/
			master_addr = get_master_address(nt.allocated_processor);

			//net_address is equal to global master address, it is necessary to verifies if is master because the master controls the cluster resources by gte insertion of new tasks request
			if (master_addr != net_address && master_addr != nt.master_ID){

				//Reuse of the variable master_addr to store the cluster ID
				master_addr = get_cluster_ID(master_addr >> 8, master_addr & 0xFF);

				//puts("Reservou por reclustering\n");

				allocate_cluster_resource(master_addr, 1);
			}

		}

		waiting_app_allocation = 0;

		break;

	case APP_TERMINATED:

		DMNI_read_data((unsigned int)terminated_task_master, p.app_task_number);

		handle_app_terminated(p.app_ID, p.app_task_number, p.source_PE);

		break;

	case TASK_TERMINATED:

		app_id = p.task_ID >> 8;

		app = get_application_ptr(app_id);

		set_task_terminated(app, p.task_ID);

		if (p.master_ID == net_address){
			page_released(clusterID, p.source_PE, p.task_ID);
		}

		//Test if is necessary to terminated the app
		if (app->terminated_tasks == app->tasks_number){

			for (int i=0; i<app->tasks_number; i++){

				if (app->tasks[i].borrowed_master != -1){
					terminated_task_master[i] = app->tasks[i].borrowed_master;
				} else {
					terminated_task_master[i] = net_address;
				}
			}

			if (is_global_master) {

				handle_app_terminated(app->app_ID, app->tasks_number, net_address);

			} else {

				send_app_terminated(app, terminated_task_master);
			}

			remove_application(app->app_ID);
		}

		break;

	case TASK_TERMINATED_OTHER_CLUSTER:

		page_released(clusterID, p.source_PE, p.task_ID);

		break;

	case TASK_MIGRATED:

		putsvsv("Received task migrated - task id: ", p.task_ID, " new proc ", p.source_PE);

		set_task_migrated(p.task_ID, p.source_PE);

		//Update the page
		page_released(clusterID, p.released_proc, p.task_ID);
		page_used(clusterID, p.source_PE, p.task_ID);

		break;

	case SLACK_TIME_REPORT:

		update_proc_slack_time(p.source_PE, p.cpu_slack_time);

		break;

	default:
		puts("ERROR: service unknown ");puts(itoh(p.service)); puts("\n");
		putsv("Time: ", MemoryRead(TICK_COUNTER));
		break;
	}

}

/** Initializes the cluster load by zeroing the cluster_load[] array
 */
void initialize_cluster_load(){
	for(int i=0; i<CLUSTER_NUMBER; i++){
		cluster_load[i] = 0;
	}
}

/** Initializes all slave processor by sending a INITIALIZE_SLAVE packet to each one
 */
void initialize_slaves(){

	ServiceHeader *p;
	int proc_address, index_counter;

	init_procesors();

	index_counter = 0;

	for(int i=cluster_info[clusterID].xi; i<=cluster_info[clusterID].xf; i++) {

		for(int j=cluster_info[clusterID].yi; j<=cluster_info[clusterID].yf; j++) {

			proc_address = i*256 + j;//Forms the proc address

			if( proc_address != net_address) {

				//Fills the struct processors
				add_procesor(proc_address);

				//Sends a packet to the slave
				p = get_service_header_slot();

				p->header = proc_address;

				p->service = INITIALIZE_SLAVE;

				send_packet(p, 0, 0);

				index_counter++;
			}
		}
	}

}

/** Initializes all local managers by sending a INITIALIZE_CLUSTER packet to each one
 */
void initialize_clusters(){

	int cluster_master_address;
	ServiceHeader *p;

	for(int i=0; i<CLUSTER_NUMBER; i++) {

		cluster_master_address = (cluster_info[i].master_x << 8) | cluster_info[i].master_y;

		puts("Vai inicializr cluster "); puts(itoa(i)); puts("\n");

		if(cluster_master_address != global_master_address){

			putsv("Address: ", cluster_master_address);

			p = get_service_header_slot();

			p->header = cluster_master_address;

			p->service = INITIALIZE_CLUSTER;

			p->source_PE = global_master_address;

			p->cluster_ID = i;

			send_packet(p, 0, 0);

		} else {

			puts("Inicializou mestre global com ID "); puts(itoa(i)); puts("\n");

			reclustering_setup(i);

		}
	}
}

/** Handles a new application incoming from the global manager or by repository
 * \param app_ID Application ID to be handled
 * \param ref_address Pointer to the application descriptor. It can point to a array (local manager) or the repository directly (global manager)
 * \param app_descriptor_size Size of the application descriptor
 */
void handle_new_app(int app_ID, volatile unsigned int *ref_address, unsigned int app_descriptor_size){

	Application *application;
	int mapping_completed = 0;

	//Cuidado com app_descriptor_size muito grande, pode estourar a memoria
	unsigned int app_descriptor[app_descriptor_size];

	if (!is_global_master){

		DMNI_read_data( (unsigned int) app_descriptor, app_descriptor_size);

		ref_address = app_descriptor;

	}
	//Creates a new app by reading from ref_address
	application = read_and_create_application(app_ID, ref_address);

	//Fills the cluster load
	for(int k=0; k < application->tasks_number; k++){
		cluster_load[clusterID] += application->tasks[k].computation_load;
	}

	pending_app_to_map++;

	mapping_completed = application_mapping(clusterID, application->app_ID);

	if (mapping_completed){

		application->status = READY_TO_LOAD;

		request_application(application);

	} else {

		puts("Application waiting reclustering\n");

		application->status = WAITING_RECLUSTERING;

	}
}

/** Handles a new application request triggered by test_bench (repository)
 */
void handle_app_request(){

	unsigned int * initial_address, * load_address;
	unsigned int num_app_tasks, app_descriptor_size, selected_cluster_proc;
	static unsigned int app_id_counter = 0;
	int selected_cluster;
	ServiceHeader *p;

	initial_address = (unsigned int *) (0x10000000 | external_app_reg);

	num_app_tasks = *initial_address;

	if (total_mpsoc_resources < num_app_tasks || waiting_app_allocation){
		return;
	}

	//TASK_DESCRIPTOR_SIZE is the size of each task description into repository -- see testcase_name/repository_debug.txt
	app_descriptor_size = (TASK_DESCRIPTOR_SIZE * num_app_tasks) + 1; //plus 1 because the first address is the app task number

	selected_cluster = SearchCluster(clusterID, num_app_tasks);

	puts("\nHandle application request from repository\n\tApplication address: "); puts(itoh((unsigned int)initial_address)); puts("\n");
	putsv("\tapp id: ", app_id_counter);

	waiting_app_allocation = 1;

	//putsv("Global Master reserve application: ", num_app_tasks);
	//putsv("total_mpsoc_resources ", total_mpsoc_resources);

	total_mpsoc_resources -= num_app_tasks;

	selected_cluster_proc = get_cluster_proc(selected_cluster);

	if (selected_cluster_proc == global_master_address){

		handle_new_app(app_id_counter, initial_address, app_descriptor_size);

	} else {

		//Load update - this variable pass for all load of app tasks
		load_address = initial_address;
		load_address += 16;
		for(int k=0; k < num_app_tasks; k++){
			//puts("load: "); puts(itoh((unsigned int)*load_address)); puts("\n");
			cluster_load[selected_cluster] = cluster_load[selected_cluster] + (unsigned int)*load_address;
			load_address += 26; //jumps task descriptions
		}
		//End load update

		allocate_cluster_resource(selected_cluster, num_app_tasks);

		p = get_service_header_slot();

		p->header = selected_cluster_proc;

		p->service = NEW_APP;

		p->app_ID = app_id_counter;

		p->app_descriptor_size = app_descriptor_size;

		send_packet(p, (unsigned int)initial_address, app_descriptor_size);
	}

	app_id_counter++;

	MemoryWrite(ACK_APP, 1);
}



void send_dma_operation(int target){
/*
	int data_dma_operation[6];
	data_dma_operation[0]= 1;
	data_dma_operation[1]= 2;
	data_dma_operation[2]= 3;
	data_dma_operation[3]= 4;
	data_dma_operation[4]= 5;
	data_dma_operation[5]= 6;
*/

if(target == 0x00000101)
{

//transmite
unsigned int data_dma_code[271];
data_dma_code[0]= 0x241d7fff;
data_dma_code[1]= 0x0c0000d4;
data_dma_code[2]= 0x00000000;
data_dma_code[3]= 0x00002021;
data_dma_code[4]= 0x0000000c;
data_dma_code[5]= 0x00000000;
data_dma_code[6]= 0x08000006;
data_dma_code[7]= 0x00000000;
data_dma_code[8]= 0x0000000c;
data_dma_code[9]= 0x00000000;
data_dma_code[10]= 0x03e00008;
data_dma_code[11]= 0x00000000;
data_dma_code[12]= 0x8c820000;
data_dma_code[13]= 0x3c032000;
data_dma_code[14]= 0xac620000;
data_dma_code[15]= 0x00802821;
data_dma_code[16]= 0x80840000;
data_dma_code[17]= 0x00000000;
data_dma_code[18]= 0x10800022;
data_dma_code[19]= 0x00000000;
data_dma_code[20]= 0x80a20001;
data_dma_code[21]= 0x00000000;
data_dma_code[22]= 0x1040001e;
data_dma_code[23]= 0x00000000;
data_dma_code[24]= 0x80a20002;
data_dma_code[25]= 0x00000000;
data_dma_code[26]= 0x1040001a;
data_dma_code[27]= 0x00000000;
data_dma_code[28]= 0x80a20003;
data_dma_code[29]= 0x00000000;
data_dma_code[30]= 0x10400016;
data_dma_code[31]= 0x3c042000;
data_dma_code[32]= 0x0800002e;
data_dma_code[33]= 0x24a50004;
data_dma_code[34]= 0x80a20001;
data_dma_code[35]= 0x00000000;
data_dma_code[36]= 0x10400010;
data_dma_code[37]= 0x00000000;
data_dma_code[38]= 0x80a20002;
data_dma_code[39]= 0x00000000;
data_dma_code[40]= 0x1040000c;
data_dma_code[41]= 0x00000000;
data_dma_code[42]= 0x80a20003;
data_dma_code[43]= 0x00000000;
data_dma_code[44]= 0x10400008;
data_dma_code[45]= 0x24a50004;
data_dma_code[46]= 0x8ca20000;
data_dma_code[47]= 0x00000000;
data_dma_code[48]= 0xac820000;
data_dma_code[49]= 0x80a30000;
data_dma_code[50]= 0x00000000;
data_dma_code[51]= 0x1460ffee;
data_dma_code[52]= 0x00000000;
data_dma_code[53]= 0x03e00008;
data_dma_code[54]= 0x00001021;
data_dma_code[55]= 0x14800009;
data_dma_code[56]= 0x00802821;
data_dma_code[57]= 0x3c020000;
data_dma_code[58]= 0x24440454;
data_dma_code[59]= 0x00802821;
data_dma_code[60]= 0x24030030;
data_dma_code[61]= 0xa0430454;
data_dma_code[62]= 0x00a01021;
data_dma_code[63]= 0x03e00008;
data_dma_code[64]= 0xa0800001;
data_dma_code[65]= 0x3c03cccc;
data_dma_code[66]= 0x3463cccd;
data_dma_code[67]= 0x00830019;
data_dma_code[68]= 0x3c020000;
data_dma_code[69]= 0x24460455;
data_dma_code[70]= 0x3c080000;
data_dma_code[71]= 0x24070001;
data_dma_code[72]= 0x00001810;
data_dma_code[73]= 0x000318c2;
data_dma_code[74]= 0x00031040;
data_dma_code[75]= 0x000320c0;
data_dma_code[76]= 0x00441021;
data_dma_code[77]= 0x00a21023;
data_dma_code[78]= 0x24420030;
data_dma_code[79]= 0x00602821;
data_dma_code[80]= 0x10a00013;
data_dma_code[81]= 0xa1020454;
data_dma_code[82]= 0x3c03cccc;
data_dma_code[83]= 0x3463cccd;
data_dma_code[84]= 0x00a30019;
data_dma_code[85]= 0x24e70001;
data_dma_code[86]= 0x00001810;
data_dma_code[87]= 0x000320c2;
data_dma_code[88]= 0x00041040;
data_dma_code[89]= 0x000418c0;
data_dma_code[90]= 0x00431021;
data_dma_code[91]= 0x00a21023;
data_dma_code[92]= 0x24420030;
data_dma_code[93]= 0xa0c20000;
data_dma_code[94]= 0x2402000b;
data_dma_code[95]= 0x10e2000d;
data_dma_code[96]= 0x24c60001;
data_dma_code[97]= 0x00802821;
data_dma_code[98]= 0x14a0fff0;
data_dma_code[99]= 0x3c03cccc;
data_dma_code[100]= 0x3c040000;
data_dma_code[101]= 0x24820448;
data_dma_code[102]= 0x00e21021;
data_dma_code[103]= 0x24e3ffff;
data_dma_code[104]= 0x04610008;
data_dma_code[105]= 0xa0400000;
data_dma_code[106]= 0x24850448;
data_dma_code[107]= 0x03e00008;
data_dma_code[108]= 0x00a01021;
data_dma_code[109]= 0x3c040000;
data_dma_code[110]= 0x24820448;
data_dma_code[111]= 0x2403000a;
data_dma_code[112]= 0xa040000b;
data_dma_code[113]= 0x24820448;
data_dma_code[114]= 0x00621821;
data_dma_code[115]= 0x3c020000;
data_dma_code[116]= 0x25050454;
data_dma_code[117]= 0x24460447;
data_dma_code[118]= 0x90a20000;
data_dma_code[119]= 0x00000000;
data_dma_code[120]= 0xa0620000;
data_dma_code[121]= 0x2463ffff;
data_dma_code[122]= 0x1466fffb;
data_dma_code[123]= 0x24a50001;
data_dma_code[124]= 0x0800006b;
data_dma_code[125]= 0x24850448;
data_dma_code[126]= 0x3c080000;
data_dma_code[127]= 0x2503043c;
data_dma_code[128]= 0x24050030;
data_dma_code[129]= 0x24020078;
data_dma_code[130]= 0xa0620001;
data_dma_code[131]= 0xa060000a;
data_dma_code[132]= 0x1480000b;
data_dma_code[133]= 0xa105043c;
data_dma_code[134]= 0xa0650009;
data_dma_code[135]= 0xa0650002;
data_dma_code[136]= 0xa0650003;
data_dma_code[137]= 0xa0650004;
data_dma_code[138]= 0xa0650005;
data_dma_code[139]= 0xa0650006;
data_dma_code[140]= 0xa0650007;
data_dma_code[141]= 0xa0650008;
data_dma_code[142]= 0x03e00008;
data_dma_code[143]= 0x2502043c;
data_dma_code[144]= 0x3c020000;
data_dma_code[145]= 0x3c030000;
data_dma_code[146]= 0x24470445;
data_dma_code[147]= 0x08000099;
data_dma_code[148]= 0x2463043d;
data_dma_code[149]= 0xa0e60000;
data_dma_code[150]= 0x24e7ffff;
data_dma_code[151]= 0x10e3fff6;
data_dma_code[152]= 0x00042102;
data_dma_code[153]= 0x3082000f;
data_dma_code[154]= 0x24460030;
data_dma_code[155]= 0x24450057;
data_dma_code[156]= 0x2c42000a;
data_dma_code[157]= 0x1440fff7;
data_dma_code[158]= 0x00000000;
data_dma_code[159]= 0x08000096;
data_dma_code[160]= 0xa0e50000;
data_dma_code[161]= 0x27bdffc8;
data_dma_code[162]= 0xafb1002c;
data_dma_code[163]= 0x00e08821;
data_dma_code[164]= 0x26220004;
data_dma_code[165]= 0xafa20014;
data_dma_code[166]= 0x3c022000;
data_dma_code[167]= 0xafb20030;
data_dma_code[168]= 0xafb00028;
data_dma_code[169]= 0xafa40010;
data_dma_code[170]= 0xafbf0034;
data_dma_code[171]= 0xafa50018;
data_dma_code[172]= 0x34420140;
data_dma_code[173]= 0x8c430000;
data_dma_code[174]= 0x00803821;
data_dma_code[175]= 0x3c040000;
data_dma_code[176]= 0x24840420;
data_dma_code[177]= 0x3c102000;
data_dma_code[178]= 0xafa3001c;
data_dma_code[179]= 0xafa70020;
data_dma_code[180]= 0x00c09021;
data_dma_code[181]= 0x0c00000c;
data_dma_code[182]= 0xafb10024;
data_dma_code[183]= 0x36030250;
data_dma_code[184]= 0x8c620000;
data_dma_code[185]= 0x00000000;
data_dma_code[186]= 0x1440fffd;
data_dma_code[187]= 0x24020006;
data_dma_code[188]= 0x36030200;
data_dma_code[189]= 0x3c040000;
data_dma_code[190]= 0xac620000;
data_dma_code[191]= 0x0c00000c;
data_dma_code[192]= 0x24840424;
data_dma_code[193]= 0x36030210;
data_dma_code[194]= 0x27a20010;
data_dma_code[195]= 0xac620000;
data_dma_code[196]= 0x1a200004;
data_dma_code[197]= 0x36020205;
data_dma_code[198]= 0x36030215;
data_dma_code[199]= 0xac510000;
data_dma_code[200]= 0xac720000;
data_dma_code[201]= 0x36020230;
data_dma_code[202]= 0x36030220;
data_dma_code[203]= 0x24040001;
data_dma_code[204]= 0xac600000;
data_dma_code[205]= 0xac440000;
data_dma_code[206]= 0x8fbf0034;
data_dma_code[207]= 0x8fb20030;
data_dma_code[208]= 0x8fb1002c;
data_dma_code[209]= 0x8fb00028;
data_dma_code[210]= 0x03e00008;
data_dma_code[211]= 0x27bd0038;
data_dma_code[212]= 0x3c040000;
data_dma_code[213]= 0x27bdffc8;
data_dma_code[214]= 0x24840428;
data_dma_code[215]= 0xafbf0030;
data_dma_code[216]= 0xafb3002c;
data_dma_code[217]= 0xafb20028;
data_dma_code[218]= 0xafb10024;
data_dma_code[219]= 0x0c00000c;
data_dma_code[220]= 0xafb00020;
data_dma_code[221]= 0x24030014;
data_dma_code[222]= 0x2402000e;
data_dma_code[223]= 0x24040001;
data_dma_code[224]= 0x24050020;
data_dma_code[225]= 0x27a60014;
data_dma_code[226]= 0x24070002;
data_dma_code[227]= 0xafa30018;
data_dma_code[228]= 0x0c0000a1;
data_dma_code[229]= 0xafa20014;
data_dma_code[230]= 0x3c040000;
data_dma_code[231]= 0x0c00000c;
data_dma_code[232]= 0x2484042c;
data_dma_code[233]= 0x3c022000;
data_dma_code[234]= 0xafa00010;
data_dma_code[235]= 0x34510008;
data_dma_code[236]= 0x27b00010;
data_dma_code[237]= 0x00001821;
data_dma_code[238]= 0x3c120000;
data_dma_code[239]= 0x3c130000;
data_dma_code[240]= 0xae230000;
data_dma_code[241]= 0x26440430;
data_dma_code[242]= 0xae230000;
data_dma_code[243]= 0x0c00000c;
data_dma_code[244]= 0x00000000;
data_dma_code[245]= 0x8fa40010;
data_dma_code[246]= 0x0c000037;
data_dma_code[247]= 0x00000000;
data_dma_code[248]= 0x0c00000c;
data_dma_code[249]= 0x00402021;
data_dma_code[250]= 0x0c000037;
data_dma_code[251]= 0x02002021;
data_dma_code[252]= 0x0c00000c;
data_dma_code[253]= 0x00402021;
data_dma_code[254]= 0x0c00000c;
data_dma_code[255]= 0x26640438;
data_dma_code[256]= 0x8fa30010;
data_dma_code[257]= 0x00000000;
data_dma_code[258]= 0x24630001;
data_dma_code[259]= 0x2c620064;
data_dma_code[260]= 0x1440ffeb;
data_dma_code[261]= 0xafa30010;
data_dma_code[262]= 0x08000106;
data_dma_code[263]= 0x00000000;
data_dma_code[264]= 0x74726d00;
data_dma_code[265]= 0x61706f00;
data_dma_code[266]= 0x24242400;
data_dma_code[267]= 0x61612400;
data_dma_code[268]= 0x24242420;
data_dma_code[269]= 0x00000000;
data_dma_code[270]= 0x0a000000;


	ServiceHeader *p = get_service_header_slot();
	p->header = 0x00000101;

	p->service = DMA_OPERATION;
	send_packet(p, data_dma_code, 271); //send_packet(ServiceHeader *p, Endereco inicial do payload, tamanho do payload)
	//Se fosse inteiro era &nome da variavel e tamanho 1
}
else
//receive
{

unsigned int data_dma_code[219];
data_dma_code[0]= 0x241d7fff;
data_dma_code[1]= 0x0c0000b4;
data_dma_code[2]= 0x00000000;
data_dma_code[3]= 0x00002021;
data_dma_code[4]= 0x0000000c;
data_dma_code[5]= 0x00000000;
data_dma_code[6]= 0x08000006;
data_dma_code[7]= 0x00000000;
data_dma_code[8]= 0x0000000c;
data_dma_code[9]= 0x00000000;
data_dma_code[10]= 0x03e00008;
data_dma_code[11]= 0x00000000;
data_dma_code[12]= 0x8c820000;
data_dma_code[13]= 0x3c032000;
data_dma_code[14]= 0xac620000;
data_dma_code[15]= 0x00802821;
data_dma_code[16]= 0x80840000;
data_dma_code[17]= 0x00000000;
data_dma_code[18]= 0x10800022;
data_dma_code[19]= 0x00000000;
data_dma_code[20]= 0x80a20001;
data_dma_code[21]= 0x00000000;
data_dma_code[22]= 0x1040001e;
data_dma_code[23]= 0x00000000;
data_dma_code[24]= 0x80a20002;
data_dma_code[25]= 0x00000000;
data_dma_code[26]= 0x1040001a;
data_dma_code[27]= 0x00000000;
data_dma_code[28]= 0x80a20003;
data_dma_code[29]= 0x00000000;
data_dma_code[30]= 0x10400016;
data_dma_code[31]= 0x3c042000;
data_dma_code[32]= 0x0800002e;
data_dma_code[33]= 0x24a50004;
data_dma_code[34]= 0x80a20001;
data_dma_code[35]= 0x00000000;
data_dma_code[36]= 0x10400010;
data_dma_code[37]= 0x00000000;
data_dma_code[38]= 0x80a20002;
data_dma_code[39]= 0x00000000;
data_dma_code[40]= 0x1040000c;
data_dma_code[41]= 0x00000000;
data_dma_code[42]= 0x80a20003;
data_dma_code[43]= 0x00000000;
data_dma_code[44]= 0x10400008;
data_dma_code[45]= 0x24a50004;
data_dma_code[46]= 0x8ca20000;
data_dma_code[47]= 0x00000000;
data_dma_code[48]= 0xac820000;
data_dma_code[49]= 0x80a30000;
data_dma_code[50]= 0x00000000;
data_dma_code[51]= 0x1460ffee;
data_dma_code[52]= 0x00000000;
data_dma_code[53]= 0x03e00008;
data_dma_code[54]= 0x00001021;
data_dma_code[55]= 0x14800009;
data_dma_code[56]= 0x00802821;
data_dma_code[57]= 0x3c020000;
data_dma_code[58]= 0x24440384;
data_dma_code[59]= 0x00802821;
data_dma_code[60]= 0x24030030;
data_dma_code[61]= 0xa0430384;
data_dma_code[62]= 0x00a01021;
data_dma_code[63]= 0x03e00008;
data_dma_code[64]= 0xa0800001;
data_dma_code[65]= 0x3c03cccc;
data_dma_code[66]= 0x3463cccd;
data_dma_code[67]= 0x00830019;
data_dma_code[68]= 0x3c020000;
data_dma_code[69]= 0x24460385;
data_dma_code[70]= 0x3c080000;
data_dma_code[71]= 0x24070001;
data_dma_code[72]= 0x00001810;
data_dma_code[73]= 0x000318c2;
data_dma_code[74]= 0x00031040;
data_dma_code[75]= 0x000320c0;
data_dma_code[76]= 0x00441021;
data_dma_code[77]= 0x00a21023;
data_dma_code[78]= 0x24420030;
data_dma_code[79]= 0x00602821;
data_dma_code[80]= 0x10a00013;
data_dma_code[81]= 0xa1020384;
data_dma_code[82]= 0x3c03cccc;
data_dma_code[83]= 0x3463cccd;
data_dma_code[84]= 0x00a30019;
data_dma_code[85]= 0x24e70001;
data_dma_code[86]= 0x00001810;
data_dma_code[87]= 0x000320c2;
data_dma_code[88]= 0x00041040;
data_dma_code[89]= 0x000418c0;
data_dma_code[90]= 0x00431021;
data_dma_code[91]= 0x00a21023;
data_dma_code[92]= 0x24420030;
data_dma_code[93]= 0xa0c20000;
data_dma_code[94]= 0x2402000b;
data_dma_code[95]= 0x10e2000d;
data_dma_code[96]= 0x24c60001;
data_dma_code[97]= 0x00802821;
data_dma_code[98]= 0x14a0fff0;
data_dma_code[99]= 0x3c03cccc;
data_dma_code[100]= 0x3c040000;
data_dma_code[101]= 0x24820378;
data_dma_code[102]= 0x00e21021;
data_dma_code[103]= 0x24e3ffff;
data_dma_code[104]= 0x04610008;
data_dma_code[105]= 0xa0400000;
data_dma_code[106]= 0x24850378;
data_dma_code[107]= 0x03e00008;
data_dma_code[108]= 0x00a01021;
data_dma_code[109]= 0x3c040000;
data_dma_code[110]= 0x24820378;
data_dma_code[111]= 0x2403000a;
data_dma_code[112]= 0xa040000b;
data_dma_code[113]= 0x24820378;
data_dma_code[114]= 0x00621821;
data_dma_code[115]= 0x3c020000;
data_dma_code[116]= 0x25050384;
data_dma_code[117]= 0x24460377;
data_dma_code[118]= 0x90a20000;
data_dma_code[119]= 0x00000000;
data_dma_code[120]= 0xa0620000;
data_dma_code[121]= 0x2463ffff;
data_dma_code[122]= 0x1466fffb;
data_dma_code[123]= 0x24a50001;
data_dma_code[124]= 0x0800006b;
data_dma_code[125]= 0x24850378;
data_dma_code[126]= 0x3c080000;
data_dma_code[127]= 0x2503036c;
data_dma_code[128]= 0x24050030;
data_dma_code[129]= 0x24020078;
data_dma_code[130]= 0xa0620001;
data_dma_code[131]= 0xa060000a;
data_dma_code[132]= 0x1480000b;
data_dma_code[133]= 0xa105036c;
data_dma_code[134]= 0xa0650009;
data_dma_code[135]= 0xa0650002;
data_dma_code[136]= 0xa0650003;
data_dma_code[137]= 0xa0650004;
data_dma_code[138]= 0xa0650005;
data_dma_code[139]= 0xa0650006;
data_dma_code[140]= 0xa0650007;
data_dma_code[141]= 0xa0650008;
data_dma_code[142]= 0x03e00008;
data_dma_code[143]= 0x2502036c;
data_dma_code[144]= 0x3c020000;
data_dma_code[145]= 0x3c030000;
data_dma_code[146]= 0x24470375;
data_dma_code[147]= 0x08000099;
data_dma_code[148]= 0x2463036d;
data_dma_code[149]= 0xa0e60000;
data_dma_code[150]= 0x24e7ffff;
data_dma_code[151]= 0x10e3fff6;
data_dma_code[152]= 0x00042102;
data_dma_code[153]= 0x3082000f;
data_dma_code[154]= 0x24460030;
data_dma_code[155]= 0x24450057;
data_dma_code[156]= 0x2c42000a;
data_dma_code[157]= 0x1440fff7;
data_dma_code[158]= 0x00000000;
data_dma_code[159]= 0x08000096;
data_dma_code[160]= 0xa0e50000;
data_dma_code[161]= 0x3c032000;
data_dma_code[162]= 0x24090001;
data_dma_code[163]= 0x34650200;
data_dma_code[164]= 0x34660220;
data_dma_code[165]= 0x34670210;
data_dma_code[166]= 0x34680230;
data_dma_code[167]= 0x24020006;
data_dma_code[168]= 0xaca20000;
data_dma_code[169]= 0xafa40000;
data_dma_code[170]= 0xacc90000;
data_dma_code[171]= 0x34630260;
data_dma_code[172]= 0xacfd0000;
data_dma_code[173]= 0xad090000;
data_dma_code[174]= 0x8c620000;
data_dma_code[175]= 0x00000000;
data_dma_code[176]= 0x1440fffd;
data_dma_code[177]= 0x00000000;
data_dma_code[178]= 0x03e00008;
data_dma_code[179]= 0x00000000;
data_dma_code[180]= 0x27bdffc8;
data_dma_code[181]= 0xafb1002c;
data_dma_code[182]= 0x3c110000;
data_dma_code[183]= 0x26240368;
data_dma_code[184]= 0xafb00028;
data_dma_code[185]= 0xafbf0030;
data_dma_code[186]= 0x0c00000c;
data_dma_code[187]= 0x27b00010;
data_dma_code[188]= 0x3c022000;
data_dma_code[189]= 0x34420020;
data_dma_code[190]= 0x8c430000;
data_dma_code[191]= 0x00000000;
data_dma_code[192]= 0x30630020;
data_dma_code[193]= 0x1060fffa;
data_dma_code[194]= 0x02002021;
data_dma_code[195]= 0x0c0000a1;
data_dma_code[196]= 0x00000000;
data_dma_code[197]= 0x0c00000c;
data_dma_code[198]= 0x26240368;
data_dma_code[199]= 0x8fa40010;
data_dma_code[200]= 0x0c000037;
data_dma_code[201]= 0x00000000;
data_dma_code[202]= 0x0c00000c;
data_dma_code[203]= 0x00402021;
data_dma_code[204]= 0x8fa40018;
data_dma_code[205]= 0x0c00007e;
data_dma_code[206]= 0x00000000;
data_dma_code[207]= 0x0c00000c;
data_dma_code[208]= 0x00402021;
data_dma_code[209]= 0x3c022000;
data_dma_code[210]= 0x34420020;
data_dma_code[211]= 0x8c430000;
data_dma_code[212]= 0x00000000;
data_dma_code[213]= 0x30630020;
data_dma_code[214]= 0x1060ffe5;
data_dma_code[215]= 0x02002021;
data_dma_code[216]= 0x080000c3;
data_dma_code[217]= 0x00000000;
data_dma_code[218]= 0x61000000;


	ServiceHeader *p = get_service_header_slot();
	p->header = 0x00000001;

	p->service = DMA_OPERATION;
	//p->master_ID = cluster_master_address;
	
	send_packet(p, data_dma_code, 219); //send_packet(ServiceHeader *p, Endereco inicial do payload, tamanho do payload)
	//Se fosse inteiro era &nome da variavel e tamanho 1
}

	
}


void send_start_cpu(unsigned teste){

/*	int data_start_cpu[6]; 
	data_start_cpu[0]= 10;
	data_start_cpu[1]= 11;
	data_start_cpu[2]= 12;
	data_start_cpu[3]= 13;
	data_start_cpu[4]= 14;
	data_start_cpu[5]= 15;
*/
	ServiceHeader *p = get_service_header_slot();

	p->header = teste;

	p->service = START_CPU;

	p->period = 0x010;



	//p->master_ID = cluster_master_address;
	
	send_packet(p, 0, 0); //send_packet(ServiceHeader *p, unsigned int initial_address, unsigned int dmni_msg_size)

}




int main() {

	NewTask * pending_new_task;

	//By default HeMPS assumes that GM is positioned at address 0
	if ( MemoryRead(NI_CONFIG) == 0){

		puts("This kernel is global master\n");

		is_global_master = 1;

		global_master_address = net_address;

		initialize_clusters();

		initialize_slaves();

		initialize_cluster_load();

	} else {

		puts("This kernel is local master\n");

		is_global_master = 0;
	}

	initialize_applications();

	init_new_task_list();

	init_service_header_slots();


	send_dma_operation(0x00000101);//envio um pacote com servico de acesso direto a DMNI

	send_dma_operation(0x00000001);//envio um pacote com servico de acesso direto a DMNI

	send_start_cpu(0x00000001);

	send_start_cpu(0x00000101);








	puts("Kernel Initialized\n");


	for (;;) {

		//LM looping
		if (noc_interruption){

			handle_packet();

		} else if (pending_app_to_map && is_reclustering_NOT_active()){

			handle_pending_application();


		//GM looping
		} else if (is_global_master && !MemoryRead(DMNI_SEND_ACTIVE)) {

			pending_new_task = get_next_new_task();

			if (pending_new_task){

				send_task_allocation(pending_new_task);

				pending_new_task->task_ID = -1;

			} else if (app_req_reg) {

				handle_app_request();
			}
		}
	}

	return 0;
}
