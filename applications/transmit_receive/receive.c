#include <libos.h>

int main() {
	volatile char *buff;
	
	buff=prepare_receive(glob_buff);
	puts("Prepared to receive\n");
	wait_receive();
	puts("Data Received\n");
	puts((char*)buff);
	
	return 0;
}
