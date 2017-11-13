#include <libos.h>
#include "map_pkg.h"

int main() {
	unsigned int i;
	char hello[] = "Hello World!\n";

	transmit(receiver, DMA_OPERATION, (unsigned int*)hello, (sizeof(hello)+3) >> 2);

	return 0;
}
