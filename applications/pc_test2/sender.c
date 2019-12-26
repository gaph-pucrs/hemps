#include <libos.h>
#include "map_pkg.h"

int main() {
	const char *hello = "Hello World!\n";

	send_msg(receiver, hello, strlen(hello)+1);
	printf("Message transmited to %X\n", receiver);
	send_msg(receiver, "END\n", strlen("END\n")+1);
	printf("Termination message sent to %X, terminating\n", receiver);

	return 0;
}
