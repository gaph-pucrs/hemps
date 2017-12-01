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

	p.target = target;
	p.service = service;
	p.length = (flit_len ? flit_len : 1);
	p.length += mem_offset(p, p.length)/sizeof(flit_t);

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
	msg_req_t msg_req, ack_reply;
	void *old_buff;
	size_t received_len;

	msg_req.addr = MemoryRead32(NET_ADDRESS);
	msg_req.size = align_type(len, flit_t)/sizeof(flit_t);

	/* waits previous operation to finish
	 * this avoids overwriting previous buffer pointer */
	old_buff = wait_receive();

	transmit(target, REQ_OPERATION, &msg_req, sizeof(msg_req_t));
	prepare_receive(&ack_reply);
	wait_receive();

	// restauring previous buffer
	MemoryWrite32(DMNI_RECEIVE_BUFFER, (size_t)old_buff & 1);

	if((ack_reply.addr != msg_req.addr))
		panic("Received ACK diverges from expected\n");

	received_len = ack_reply.size*sizeof(flit_t);
	len = min(received_len, len);
	transmit(target, DMA_OPERATION, msg, len);
}

typedef union {
	uint32_t i32;
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

void *receive_msg(flit_t *src, size_t *size) {
	prepare_recv_msg(src, size);
	return wait_receive();
}
