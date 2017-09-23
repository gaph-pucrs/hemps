#include <api.h>
#include <stdlib.h>

char message[] = "Hello World\n";

int main(){
	int i;
	
	Echo("task A started.");
	Echo(itoa(GetTick()));
	for(i=0;i<500;i++){
		Transmit(ROUTER_ADDR(0, 0)|MANUAL_WEST, message, 3);
	}
	Echo(itoa(GetTick()));
	Echo("task A finished.");
	exit();
}
