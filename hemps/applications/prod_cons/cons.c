/*
 * cons.c
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
	volatile int p;

	Echo("Inicio da aplicacao cons");
	Echo(itoa(GetTick()));

	for(i=0; i<PROD_CONS_ITERATIONS; i++){

		Receive(&msg, prod);
	}


	Echo("Fim da aplicacao cons");
	Echo(itoa(GetTick()));

	exit();

}


