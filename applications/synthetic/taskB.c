
#include <libos.h>
#include "map_pkg.h"
#include "syn_std.h"
int main()
{
	
	int i, j,t;
	int msg1[30];
	int size;

    	puts("synthetic task B started.\n");
	
	for(i=0;i<SYNTHETIC_ITERATIONS;i++){
	for(t=0;t<1000;t++){
	}	
	size = 30;
	for(j=0;j<30;j++) msg1[j]=i;
	send_msg(taskC, msg1, size);
	}
    puts("synthetic task B finished.\n");
    exit();
}
