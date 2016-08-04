/*
 * prod.c
 *
 *  Created on: 07/03/2013
 *      Author: mruaro
 */

#include <api.h>
#include <stdlib.h>
#include "prod_cons_std.h"

Message msg;

int main()
{

	int i;
	volatile int t;


	Echo("Inicio da aplicacao prod");
	Echo(itoa(GetTick()));

	for(i=0;i<128;i++) msg.msg[i]=i;
	msg.length = 10;

	msg.msg[9] = 0xB0A;

	for(i=0; i<PROD_CONS_ITERATIONS; i++){
		Send(&msg, cons);
	}


	Echo("Fim da aplicacao prod");
	Echo(itoa(GetTick()));
	exit();

}


