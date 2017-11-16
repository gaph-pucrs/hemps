#include "include/libos.h"

static void *send_buff = NULL;

void transmit(flit_t target, flit_t service,
							void *msg, size_t msg_len) {
	static const flit_t zero = 0;
	static mac_header_t p;
	size_t flit_len, len;

	// Waiting untill last transmission has finished
	while (MemoryRead32(DMNI_SEND_ACTIVE));

	len = align_type(msg_len, flit_t);
	flit_len = len/sizeof(flit_t);

	p.header = target;
	p.service = service;
	p.payload_size = (MAC_HEADER_LEN-2) + (flit_len ? flit_len : 1);

	// Configure DMNI for package header transmission
	MemoryWrite32(DMNI_SIZE, MAC_HEADER_LEN);
	MemoryWrite32(DMNI_ADDRESS, (size_t)(&p));

	// Configure DMNI for package payload transmission
	if(flit_len) {
		free(send_buff);

		if(!(send_buff = malloc(len)))
			panic("Cannot allocate memory for transmit buffer.\n");
		
		memcpy(send_buff, msg, msg_len);
		if(len - msg_len)
			memset(send_buff+msg_len, 0, len-msg_len);
		
		MemoryWrite32(DMNI_SIZE_2, flit_len);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)send_buff);
	} else {
		MemoryWrite32(DMNI_SIZE_2, 1);
		MemoryWrite32(DMNI_ADDRESS_2, (size_t)(&zero));
	}

	MemoryWrite32(DMNI_OP, READ);
	MemoryWrite32(DMNI_START, 1);
}

void *prepare_receive(void *buff) {	
	buff = (void*)align((size_t)buff, 2);
	MemoryWrite32(DMNI_RECEIVE_BUFFER, (size_t)buff);
	return buff;
}

void *wait_receive(){
	size_t ret;
	while(!((ret = MemoryRead32(DMNI_RECEIVE_BUFFER)) & 1));
	return (void*)(ret & -4);
}

void send_msg(flit_t target, void *msg, size_t len) {
	msg_req_t msg_req;

	msg_req.addr = MemoryRead32(NET_ADDRESS);
	msg_req.size = (len+3) >> 2;
	
	transmit(target, REQ_OPERATION, &msg_req, sizeof(msg_req_t));
	prepare_receive(&msg_req);
	wait_receive();

	transmit(target, DMA_OPERATION, msg, len);
}

typedef union {
	uint32_t i32;
	uint16_t i16[2];
	msg_req_t s;
} msg_req_u;

void *prepare_recv_msg(flit_t *src, size_t *size) {
	static msg_req_u req;
	void *buff;

	while(!(req.i32 = MemoryRead32(DMNI_REQ_FIFO)));
	MemoryWrite32(DMNI_REQ_FIFO, 0);

	if(!(buff = malloc(req.s.size << 2)))
		return NULL;

	if(src) *src = req.s.addr;
	if(size) *size = req.s.size << 2;
	
	transmit(req.s.addr, DMA_OPERATION, &req, sizeof(msg_req_t));

	return prepare_receive(buff);
}
