
#include <libos.h>
#include "map_pkg.h"
#include "syn_std.h"
int main()
{
	
	int i, j,t;
 	int msg1[30];
 	int size;
	int *buff;
	int src;
	puts("synthetic task C started.\n");

	//msg1 = malloc(sizeof(int));

for(i=0;i<SYNTHETIC_ITERATIONS;i++){
	
	size = 30;
	for(j=0;j<30;j++) msg1[j]=i;
	prepare_recv_msg(&src, &size);
    	buff = wait_receive();

	for(t=0;t<1000;t++){
	}
	
	send_msg(taskD, (unsigned int*)msg1, sizeof(msg1));

	prepare_recv_msg(&src, &size);
    	buff = wait_receive();
	
	for(t=0;t<1000;t++){
	}
	send_msg(taskE, (unsigned int*)msg1, sizeof(msg1));
}
    puts("synthetic task C finished.\n");
    exit();

}
