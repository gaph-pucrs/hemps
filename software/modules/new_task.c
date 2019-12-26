/*!\file new_task.c
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief This module implements the function of NewTask FIFO.
 * \detailed This module is used only by the manager kernel
 */

#include "new_task.h"
#include "utils.h"

NewTask new_task_list[MAX_GLOBAL_TASKS];	//!<Array with the new task requisitions

unsigned int first = 0;	//!<pointer to the first position index to remove in the array
unsigned int last = 0;		//!<pointer to the last position index to insert in the array

unsigned int size_new_app = 0;

/**Initializes the new_task_list array
 */
void init_new_task_list(){

	for(int i=0; i<MAX_GLOBAL_TASKS; i++){
		new_task_list[i].task_ID = -1;
	}
}

/**Add a new task
 * \param input Task to be added
 */
void add_new_task(NewTask *input){

	NewTask *new_task = &new_task_list[last];

	if (size_new_app == MAX_GLOBAL_TASKS){
		puts("ERROR: new task overflow\n");
		while(1);
	}

	if (last == MAX_GLOBAL_TASKS-1){
		last = 0;
	} else {
		last++;
	}

	new_task->task_ID = input->task_ID;

	new_task->master_ID = input->master_ID;

	new_task->allocated_processor = input->allocated_processor;

	new_task->initial_address = input->initial_address;

	new_task->code_size = input->code_size;

	size_new_app++;

}

/**Remove the next new task positioned at first index
 * \return Pointer of the next task
 */
NewTask * get_next_new_task(){

	NewTask *new_task;

	if (size_new_app == 0){

		return 0;

	}

	new_task = &new_task_list[first];

	if (first == MAX_GLOBAL_TASKS-1){
		first = 0;
	} else {
		first++;
	}

	size_new_app--;

	return new_task;

}
