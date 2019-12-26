/*!\file new_task.h
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Created by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief This module defines the structure and some the function of NewTask FIFO.
 * \detailed The NewTask structure stores information about new task requests received by the manager kernel.
 */

#ifndef SOFTWARE_INCLUDE_NEW_TASK_NEW_TASK_H_
#define SOFTWARE_INCLUDE_NEW_TASK_NEW_TASK_H_

#include "../../include/kernel_pkg.h"

//#define MAX_GLOBAL_TASKS	(MAX_LOCAL_TASKS * MAX_SLAVE_PROCESSORS) + 1	//!<Max number of tasks that can execute simultaneously in the system. (Plus one to first never be equal to last)
#define MAX_GLOBAL_TASKS	MAX_TASKS_APP + 1	//!<Changed: as only one app can being mapped, the number of requesting task is not higher than MAX_TASKS_APP (Plus one to first never be equal to last)

/**
 * \brief This structure stores the variables used to manage a new task requisition by the global manager kernel
 */
typedef struct {
	int task_ID;					//!<ID of the new task
	int master_ID;					//!<Master address (XY) of the task
	int allocated_processor;		//!<Address (XY) of the task allocated processor
	unsigned int initial_address;	//!<Initial repository address of the task
	int code_size;					//!<Size of the repository code in repository words (4 bytes)
} NewTask;


void init_new_task_list();

void add_new_task(NewTask *);

NewTask * get_next_new_task();


#endif /* SOFTWARE_INCLUDE_NEW_TASK_NEW_TASK_H_ */
