#include <libos.h>

int main() {
	char *msg = NULL;
	flit_t src;
	size_t size;

	do {
		free(msg);
		prepare_recv_msg(&src, &size);
		printf("Waiting to receive %d bytes from %X\n", size, src);
		msg = wait_receive();
		puts(msg);
	} while(strcmp(msg, "END\n"));
	
	return 0;
}
