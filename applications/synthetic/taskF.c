
#include <libos.h>
#include "map_pkg.h"
#include "syn_std.h"
int main()
{

	int i,t;
 	int msg1[30];
 	int size;
	int src;
	int *buff;

    puts("synthetic task F started.\n");

for(i=0;i<SYNTHETIC_ITERATIONS;i++){
	
		prepare_recv_msg(&src, &size);
    		buff = wait_receive();
		
			for(t=0;t<1000;t++){
		}
		prepare_recv_msg(&src, &size);
    		buff = wait_receive();
		
	}

	puts("synthetic task F finished.\n");
	exit();

}
