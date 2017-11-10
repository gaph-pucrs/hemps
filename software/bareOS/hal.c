#include "include/libos.h"

typedef struct {
	flit_t source;
	flit_t retcode;
} exit_msg_t;

void exit(int code) {
	exit_msg_t msg;

	msg.source = MemoryRead32(NET_ADDRESS);
	msg.retcode = code;

	transmit(MemoryRead32(LOADER_NETADDR), EXIT_OPERATION, &msg, sizeof(exit_msg_t));
	
	for(;;) MemoryWrite32(CPU_KILL, CPU_KILL_MAGIC);
}

void putchar(char c) {
	MemoryWrite32(UART_WRITE, (uint32_t)c);
}

void puts(const char *string) {
	int *str_part;
	//This is the most crazy and complicated FOR declaration that I ever seen. For obviously purposes, I divided the FOR section in lines
	//PS: This indicates a hardware developer putting its hands on software development
	//PS2: The original author need to review demorgans law for boolean simplification
	for(str_part = (int*)string,  MemoryWrite32(UART_WRITE, *str_part);
			((char*)str_part)[0] && ((char*)str_part )[1] && ((char*)str_part )[2] && ((char*)str_part)[3];
			MemoryWrite32(UART_WRITE, *(++str_part)));
}
