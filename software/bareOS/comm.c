#include "include/libos.h"

void transmit(flit_t target, flit_t service,
							void *msg, size_t len) {
	static const flit_t zero = 0;
	static mac_header_t p;

	len = align_type(len, flit_t);
	
	p.header = target;
	p.payload_size = (MAC_HEADER_LEN-2) + (len ? len : 1);     // 3 devido ao service header  - 2 flits da noc
	p.service = service;

	// Configure DMNI for package header transmission
	MemoryWrite32(DMNI_SIZE, MAC_HEADER_LEN);
	MemoryWrite32(DMNI_ADDRESS, (size_t)(&p) );

	// Configure DMNI for package payload transmission
	if(len) {
		MemoryWrite32(DMNI_SIZE_2, len);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)msg);
	} else {
		MemoryWrite32(DMNI_SIZE_2, 1);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)(&zero));
	}

	MemoryWrite32(DMNI_OP, READ);
	MemoryWrite32(DMNI_START, 1);

	while (MemoryRead32(DMNI_SEND_ACTIVE));
}

volatile void *prepare_receive(volatile void *buff) {
	buff = (void*)align((size_t)buff, 2);
	MemoryWrite32(DMNI_RECEIVE_BUFFER, (size_t)buff);
	return buff;
}

void wait_receive(){
	while(!(MemoryRead32(DMNI_RECEIVE_BUFFER) & 1));
}
