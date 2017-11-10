#ifndef _COMM_H
#define _COMM_H

#include "prototypes.h"

/* Manual Routing destination ports */
#define MANUAL_EAST  0x80000000
#define MANUAL_WEST  0xA0000000
#define MANUAL_NORTH 0xC0000000
#define MANUAL_SOUTH 0xE0000000

#define ROUTER_ADDR(x, y) (((x<<8)|y)&0xFFFF)
#define MAC_HEADER_LEN (sizeof(mac_header_t)/sizeof(flit_t))

#define EXIT_OPERATION 0x70
#define DMA_OPERATION  0x290
#define REQ_OPERATION  0x480

typedef uint32_t flit_t;

typedef struct {
	flit_t header;
	flit_t payload_size;
	flit_t service;
} mac_header_t;

void transmit(flit_t target, flit_t service,
							void *msg, size_t len);

volatile void *prepare_receive(volatile void *buff);
void wait_receive();

#endif /* !_COMM_H */
