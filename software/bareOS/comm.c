#include "include/libos.h"

static void *send_buff = NULL;

void transmit(flit_t target, flit_t service,
							void *msg, size_t len) {
	static const flit_t zero = 0;
	static mac_header_t p;

	while (MemoryRead32(DMNI_SEND_ACTIVE));

	len = (len + 3) >> 2;
	p.header = target;
	p.payload_size = (MAC_HEADER_LEN-2) + (len ? len : 1);
	p.service = service;

	// Configure DMNI for package header transmission
	MemoryWrite32(DMNI_SIZE, MAC_HEADER_LEN);
	MemoryWrite32(DMNI_ADDRESS, (size_t)(&p) );

	// Configure DMNI for package payload transmission
	if(len) {
		free(send_buff);
		if(!(send_buff = malloc(len<<2)))
			panic("Cannot allocate memory for transmit buffer.\n");
		memcpy(send_buff, msg, len<<2);
		MemoryWrite32(DMNI_SIZE_2, len);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)send_buff);
	} else {
		MemoryWrite32(DMNI_SIZE_2, 1);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)(&zero));
	}

	MemoryWrite32(DMNI_OP, READ);
	MemoryWrite32(DMNI_START, 1);
}

void *prepare_receive(void *buff) {
	if((size_t)buff & 3)
		panic("unalighned buffer %p", buff);
	
	buff = (void*)align((size_t)buff, 2);
	MemoryWrite32(DMNI_RECEIVE_BUFFER, (size_t)buff);
	return buff;
}

void *wait_receive(){
	uint32_t ret;
	while(!((ret = MemoryRead32(DMNI_RECEIVE_BUFFER)) & 1));
	return (void*)(ret & -4);
}

void send_msg(flit_t target, void *msg, size_t len) {
	msg_req_t msg_req;
	volatile msg_req_t *ack;

	msg_req.addr = MemoryRead32(NET_ADDRESS);
	msg_req.size = (len+3) >> 2;
	
	transmit(target, REQ_OPERATION, &msg_req, sizeof(msg_req_t));
	prepare_receive(&msg_req);
	ack = wait_receive();

	transmit(target, DMA_OPERATION, msg, len);
}

void *prepare_recv_msg(flit_t *src, size_t *size) {
	uint32_t reqi;
	msg_req_t *req = (msg_req_t*)&reqi;
	void *buff;

	while(!(reqi = MemoryRead32(DMNI_REQ_FIFO)));

	printf("read %X from REQ_FIFO at %p\n", reqi, req);

	if(!(buff = malloc(req->size << 2))) {
		puts("Could not allocated memory for message reception\n");
		return NULL;
	}

	prepare_receive(buff);

	if(src) *src = req->addr;
	if(size) *size = req->size;
	
	transmit(req->addr, DMA_OPERATION, req, sizeof(msg_req_t));

	return buff;
}
