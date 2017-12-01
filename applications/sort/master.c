#include <libos.h>
#include "map_pkg.h"

#include "sort.h"

#define TASKS 40
#define SLAVES 3

void init_array(int *array, int size){
	int i;

	for (i = 0; i < size; i++)
		array[i] = size - i;
}

void print_array(int *array, int size){
	int i;
	for (i = 0; i < size; i++) {
		puts(itoasc(array[i]));
		puts(" ");
	}
	puts("\n");

}

int get_id(int src) {
	if (src==slave1)
		return 0;
	if (src==slave2)
		return 1;
	if (src==slave3)
		return 2;	
}

int main(){
	int i;
	int *buff;
	int slave_addr[SLAVES] = {slave1, slave2, slave3};

	int array[TASKS][ARRAY_SIZE];
	int slave_task[SLAVES];
	int task = 0;
	int slave_id;

	int msg_kill = KILL_PROC;
	flit_t src;
	size_t size;

	for (i = 0; i < TASKS; i++)
		init_array(array[i], ARRAY_SIZE);

	for (i = 0; i < SLAVES; i++){
		prepare_recv_msg(&src, &size);
		printf("Waiting to receive %d bytes from slave %X\n", size, src);
		buff = wait_receive();
		puts(itoasc(buff[0]));
		puts("\n");
		slave_task[buff[0]] = task;
		send_msg(slave_addr[buff[0]], (unsigned int*)array[task], sizeof(array[task]));
		free(buff);
		task++;
	}

	for (i = 0; i < TASKS; i++){
		prepare_recv_msg(&src, &size);
		printf("Waiting to receive %d bytes from slave %X\n", size, src);
		buff = wait_receive();
		print_array(buff, ARRAY_SIZE);
		free(buff);
		slave_id = get_id(src);
		slave_task[slave_id] = task;
		if (task == TASKS){
			send_msg(slave_addr[slave_id], &msg_kill, sizeof(msg_kill));
			printf("Master Sening kill to %X\n", src);
		}
		else {
			send_msg(slave_addr[slave_id], (unsigned int*)array[task], sizeof(array[task]));
			task++;
		}
	}
}
