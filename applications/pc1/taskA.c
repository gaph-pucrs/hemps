#include <api.h>
#include <stdlib.h>
Message msg;
int main(){
	int i, j,t;
	Echo("task A started.");
	Echo(itoa(GetTick()));
	for(i=0;i<1;i++)
	{
		msg.length = 30;
		for(j=0;j<30;j++) msg.msg[j]=i;
		Send(&msg,taskB);
	}

	Echo(itoa(GetTick()));
	Echo("task A finished.");
	exit();
}
