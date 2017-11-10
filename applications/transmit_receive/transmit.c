#include <libos.h>

int main() {
	unsigned int i;
	char hello[] = "Hello World!\n";

	transmit(ROUTER_ADDR(0,1), DMA_OPERATION, (unsigned int*)hello, (sizeof(hello)+3) >> 2);

	return 0;
}
