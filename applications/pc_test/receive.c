#include <libos.h>

int main() {
	volatile char *buff;
	
	buff=prepare_receive(malloc(256));
	puts("Prepared to receive\n");
	wait_receive();
	puts("Data Received\n");
	puts((char*)buff);
	
	return 0;
}
