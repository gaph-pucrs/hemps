#include <libos.h>

int main() {

	send_msg(ROUTER_ADDR(0, 1), "HELLO WORLD\n", strlen("HELLO WORLD\n")+1);
	send_msg(ROUTER_ADDR(0, 1), "END", strlen("END")+1);

	return 0;
}
