/*********************************************************************
* Filename:   aes_sl(n).c
* Author:     Leonardo Rezende Juracy and Luciano L. Caimi
* Copyleft:    
* Disclaimer: This code is presented "as is" without any guarantees.
* Details:   
*********************************************************************/

/*************************** HEADER FILES ***************************/
#include <libos.h>
#include "map_pkg.h"
#include "aes.h"
/**************************** VARIABLES *****************************/

int *msg;

/*************************** MAIN PROGRAM ***************************/

int main()
{
	unsigned int key_schedule[60];
	int qtd_messages, op_mode, x, flag=1, id = -1, i;
	int size;
	int src;
	int msg1[4*AES_BLOCK_SIZE];
	unsigned int enc_buf[128];
	unsigned int input_text[16]; 
	unsigned int key[1][32] = {
		{0x60,0x3d,0xeb,0x10,0x15,0xca,0x71,0xbe,0x2b,0x73,0xae,0xf0,0x85,0x7d,0x77,0x81,0x1f,0x35,0x2c,0x07,0x3b,0x61,0x08,0xd7,0x2d,0x98,0x10,0xa3,0x09,0x14,0xdf,0xf4}
	};

    puts("task AES SLAVE started - ID:\n"); 
	aes_key_setup(&key[0][0], key_schedule, 256);    
    
    while(flag){
    	prepare_recv_msg(&src, &size);
    	msg = wait_receive();
		memcpy(input_text, msg, 12);
			
#ifdef debug_comunication_on
	puts(" ");  
	puts("Slave configuration\n");
	for(i=0; i<3;i++){
	}
	puts(" "); 
#endif 
				
		op_mode = input_text[0];
		qtd_messages = input_text[1];
		x = input_text[2];	
		
		if(id == -1){
				id = x;
		}	
		puts("Operation:\n"); 
		puts("Blocks:\n"); 		

		if (op_mode == END_TASK){
			flag = 0;
			qtd_messages = 0;
		}
		for(x = 0; x < qtd_messages; x++){
			prepare_recv_msg(&src, &size);
    		msg = wait_receive();
			memcpy(input_text, msg, 4*AES_BLOCK_SIZE);
			
#ifdef debug_comunication_on
	puts(" ");  
	puts("received msg");
	for(i=0; i<16;i++){
	}
	puts(" "); 
#endif 
			
			if(op_mode == CIPHER_MODE){
				puts("encript");				
				aes_encrypt(input_text, enc_buf, key_schedule, KEY_SIZE);	
			}
			else{
				puts("decript");					
				aes_decrypt(input_text, enc_buf, key_schedule, KEY_SIZE);
			}			
			size = 4*AES_BLOCK_SIZE;
			memcpy( msg1, enc_buf,4*AES_BLOCK_SIZE);
			send_msg(aes_master, (unsigned int*)msg1, sizeof(msg1));
		}
	}
    puts("task AES SLAVE finished  - ID: \n");
    exit();
}
