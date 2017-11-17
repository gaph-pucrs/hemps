#include <libos.h>
#include "map_pkg.h"

#include "sort.h"

#define SLAVE_ID 0

int main(){
	int task_request[2];
	int *array;

	flit_t src;
	size_t size;

	task_request[0] = SLAVE_ID;
	task_request[1] = TASK_REQUEST;
    /*Requests initial task*/
    send_msg(master, &task_request, 2*4);

    /* Wait for a task, execute and return result to master*/
    for (;;) {
    	prepare_recv_msg(&src, &size);
    	array=wait_receive();
    	if (size < ARRAY_SIZE*4) break;
		quicksort(array, 0, ARRAY_SIZE-1);
		send_msg(master, (unsigned int*)array, ARRAY_SIZE*4);
    }
}