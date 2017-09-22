/*!\file api.h
 * HEMPS VERSION - 8.0 - support for RT applications
 *
 * Distribution:  June 2016
 *
 * Edited by: Marcelo Ruaro - contact: marcelo.ruaro@acad.pucrs.br
 *
 * Research group: GAPH-PUCRS   -  contact:  fernando.moraes@pucrs.br
 *
 * \brief
 * Implements the API for the user's task and defines the structure Message,
 * used by tasks to exchange messages
 */

#ifndef __TASK_H__
#define __TASK_H__

/* Syscalls*/
#define EXIT      			0
#define WRITEPIPE 			1
#define READPIPE  			2
#define GETTICK   			3
#define ECHO      			4
#define	REALTIME			5
#define TRANSMIT 			6
	
#define MemoryWrite(A,V) *(volatile unsigned int*)(A)=(V)
#define TRUE	1
#define FALSE	0

/* Manual Routing destination ports */
#define MANUAL_EAST  0x80000000
#define MANUAL_WEST  0xA0000000
#define MANUAL_NORTH 0xC0000000
#define MANUAL_SOUTH 0xE0000000

#define ROUTER_ADDR(x, y) (((x<<8)|x)&0xFFFF)

extern int SystemCall();

#define Send(msg, target) while(!SystemCall(WRITEPIPE, (unsigned int*)msg, target,0))
#define Receive(msg, source) while(!SystemCall(READPIPE, (unsigned int*)msg, source,0))
#define GetTick() SystemCall(GETTICK,0,0,0)
#define Echo(str) SystemCall(ECHO, (char*)str,0,0)
#define exit() while(!SystemCall(EXIT, 0, 0, 0))

// Bruno's modification 14/09 - The change reflects the need of a transit message to send packets outside the NoC.
// The call is Target (ROUTER_ADDR(x,y)|MANUAL_PORT) , message (payload), payload len in flits)
#define Transmit(target, msg, len) while (!SystemCall(TRANSMIT, target, (unsigned int*)msg, len))

//Real-Time API - time represented in microseconds
#define RealTime(period, deadline, execution_time) while(!SystemCall(REALTIME, period, deadline, execution_time))

/*--------------------------------------------------------------------
 * struct Message
 *
 * DESCRIPTION:
 *    Used to handle messages inside the task.
 *    This is not the same structure used in the kernels.
 *
 *--------------------------------------------------------------------*/
#define MSG_SIZE 128

typedef struct {
	int length;
	int msg[MSG_SIZE];
} Message;

#endif /*__TASK_H__*/

