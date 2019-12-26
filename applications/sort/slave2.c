#include <libos.h>
#include "map_pkg.h"

#include "sort.h"

#define SLAVE_ID 1

int main(){
	int task_request[2];
	int *array;

	flit_t src;
	size_t size;

	task_request[0] = SLAVE_ID;
	task_request[1] = TASK_REQUEST;
    /*Requests initial task*/
    send_msg(master, &task_request, sizeof(task_request));

    /* Wait for a task, execute and return result to master*/
    for (;;) {
    	prepare_recv_msg(&src, &size);
    	array=wait_receive();
    	if (array[0] == KILL_PROC) break;
		quicksort(array, 0, ARRAY_SIZE-1);
		send_msg(master, (unsigned int*)array, ARRAY_SIZE*4);
    }
}