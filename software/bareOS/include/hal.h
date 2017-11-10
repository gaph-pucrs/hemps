#ifndef _HAL_H
#define _HAL_H

#include "prototypes.h"

/* Paramenters used by malloc */
//pointer to the end of static program memory
extern size_t _end;
//assumes _stack points to the end of the memory
extern size_t _stack;
#define MALLOC_ALIGNMENT 4

#define HEAP_START ((void*)&_end)
#define HEAP_END ((void*)((&_stack)-1024)) // stack - 4kB
#define HEAP_ALLOCATION_GRAIN 10 //1kB

/*********** Hardware addresses ***********/
#define UART_WRITE        	0x20000000
#define UART_READ         	0x20000000
#define IRQ_MASK          	0x20000010
#define IRQ_STATUS        	0x20000020
#define TIME_SLICE       	0x20000060
#define SYS_CALL		   	0x20000070
#define END_SIM 		   	0x20000080
#define CLOCK_HOLD 		   	0x20000090

/* Network Interface*/
#define	NI_STATUS_RECV		0x20000100
#define	NI_STATUS_SEND		0x20000110
#define	NI_RECV				0x20000120
#define	NI_SEND				0x20000130
#define	NI_CONFIG			0x20000140
#define	NI_ACK				0x20000150
#define	NI_NACK				0x20000160
#define	NI_END				0x20000170
#define	CURRENT_PAGE		0x20000180
#define NEXT_PAGE			0x20000190

/* Network information */
#define	NET_ADDRESS			0x20000140
#define LOADER_NETADDR  0x20000144

/* Task termination */
#define CPU_KILL 0x20000320
#define CPU_KILL_MAGIC 0xDEADBEAF

/* DMNI */
#define DMNI_SIZE_2				0x20000205
#define DMNI_ADDRESS_2 			0x20000215
#define DMNI_SIZE		  		0x20000200
#define DMNI_ADDRESS		  	0x20000210
#define DMNI_OP			  		0x20000220
#define DMNI_START		  		0x20000230
#define DMNI_ACK			  	0x20000240
#define DMNI_SEND_ACTIVE	  	0x20000250
#define DMNI_RECEIVE_ACTIVE		0x20000260
#define DMNI_RECEIVE_BUFFER   0x20000264

//Scheduling report
#define SCHEDULING_REPORT	0x20000270
#define INTERRUPTION		0x10000
//#define SYSCALL			0x20000
#define SCHEDULER			0x40000
#define IDLE				0x80000

//Communication graphical debbug
#define ADD_PIPE_DEBUG			0x20000280
#define REM_PIPE_DEBUG			0x20000285
#define ADD_REQUEST_DEBUG		0x20000290
#define REM_REQUEST_DEBUG		0x20000295

/* DMNI operations */
#define READ	0
#define WRITE	1

#define TICK_COUNTER	  	0x20000300
#define CURRENT_TASK	  	0x20000400

#define REQ_APP		  		0x20000350
#define ACK_APP		  		0x20000360

#define SLACK_TIME_MONITOR		0x20000370

//Kernel pending service FIFO
#define PENDING_SERVICE_INTR	0x20000400

#define SLACK_TIME_WINDOW		50000 // half milisecond

/*********** Interrupt bits **************/
#define IRQ_PENDING_SERVICE			0x01 //bit 0
#define IRQ_SLACK_TIME				0x02 //bit 1
#define IRQ_SCHEDULER				0x08 //bit 3
#define IRQ_NOC					 	0x20 //bit 5
         
/*Memory Access*/
#define MemoryRead32(A) (*(volatile uint32_t*)(A))
#define MemoryWrite32(A,V) *(volatile uint32_t*)(A)=(V)
#define MemoryRead16(A) (*(volatile uint16_t*)(A))
#define MemoryWrite16(A,V) *(volatile uint16_t*)(A)=(V)
#define MemoryRead8(A) (*(volatile uint8_t*)(A))
#define MemoryWrite8(A,V) *(volatile uint8_t*)(A)=(V)

// Stubs, need real implementation if running on real hardware
static inline int enter_critical() { return 0; }
static inline void leave_critical(int i) {}


#endif
