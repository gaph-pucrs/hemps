#include <libos.h>

int main() {
	char *msg;
	flit_t src;
	size_t size;

	do {
		prepare_recv_msg(&src, &size);
		puts("Prepared to receive\n");
		msg = wait_receive();
		printf("%d bytes received from %X\n", size, src);
		puts(msg);
		free(msg);
	} while(strcmp(msg, "END"));
	
	return 0;
}
