

#define MemoryWrite(A,V) *(volatile unsigned int*)(A)=(V)
#define MemoryRead(A) (*(volatile unsigned int*)(A))


/*********** Hardware addresses ***********/
#define IRQ_MASK            0x20000010
#define IRQ_STATUS          0x20000020


/*********** Interrupt bits **************/
#define IRQ_PENDING_SERVICE     0x01 //bit 0
#define IRQ_SLACK_TIME        0x02 //bit 1
#define IRQ_SCHEDULER       0x08 //bit 3
#define IRQ_NOC           0x20 //bit 5

/* DMNI operations */
#define READ	0
#define WRITE	1


#define	NI_CONFIG			0x20000140

/* DMNI*/
#define DMNI_SIZE_2				0x20000205
#define DMNI_ADDRESS_2 			0x20000215
#define DMNI_SIZE		  		0x20000200
#define DMNI_ADDRESS		  	0x20000210
#define DMNI_OP			  		0x20000220
#define DMNI_START		  		0x20000230
#define DMNI_ACK			  	0x20000240
#define DMNI_SEND_ACTIVE	  	0x20000250
#define DMNI_RECEIVE_ACTIVE		0x20000260

/* Manual Routing destination ports */
#define MANUAL_EAST  0x80000000
#define MANUAL_WEST  0xA0000000
#define MANUAL_NORTH 0xC0000000
#define MANUAL_SOUTH 0xE0000000

#define ROUTER_ADDR(x, y) (((x<<8)|x)&0xFFFF)
#define noc_interruption    (MemoryRead(IRQ_STATUS) & IRQ_NOC)  //!< Signals a incoming packet from NoC


#define MESSAGE_DELIVERY  0x00000020
#define CONSTANT_PKT_SIZE 6 //Tamanho do pacote do header


#define UART_WRITE 0x20000000
#define TESTE 0x20000008

typedef struct {
	unsigned int header;				
	unsigned int payload_size;			
	unsigned int service;				
	unsigned int source;				
	unsigned int target;
	unsigned int pkt_size;				
} ServiceHeader;






int puts(char *string) {

	int *str_part;
	//This is the most crazy and complicated FOR declaration that I ever seen. For obviously purposes, I divided the FOR section in lines
	//PS: This indicates a hardware developer putting its hands on software development
	for(
			str_part = (int*)string,  MemoryWrite(UART_WRITE,*str_part);

			!( ( (char*)str_part )[0] == 0 || ( (char*)str_part )[1] == 0 || ( (char*)str_part )[2] == 0 || ( (char*)str_part )[3] == 0);

			*str_part++,MemoryWrite(UART_WRITE, *str_part)
	);
	return 0;
}

/**Converts a integer number to its decimal representation in a array of char
 * \param num Integer number to be converted
 * \return Array of chars
 */
char *itoa(unsigned int num)
{
   static char buf[12];
   static char buf2[12];
   int i,j;

   if (num==0)
   {
      buf[0] = '0';
      buf[1] = '\0';
      return &buf[0];
   }

   for(i=0;i<11 && num!=0;i++)
   {
      buf[i]=(char)((num%10)+'0');
      num/=10;
   }
   buf2[i] = '\0';
   j = 0;
   i--;
   for(;i>=0;i--){
         buf2[i]=buf[j];
         j++;
   }
   return &buf2[0];
}



char *itoh(unsigned int num)
{
   static char buf[11];
   int i;
   buf[10]=0;

   buf[0] = '0';
   buf[1] = 'x';

   if (num==0)
   {
      buf[2] = '0';
      buf[3] = '0';
      buf[4] = '0';
      buf[5] = '0';
      buf[6] = '0';
      buf[7] = '0';
      buf[8] = '0';
      buf[9] = '0';
      return buf;
   }

   for(i=9;i>=2;--i)
   {
      if ((num%16) < 10)
         buf[i]=(char)((num%16)+'0');
      else
         buf[i]=(char)((num%16)+'W');

      num/=16;
   }

   return buf;
}




void transmite(int target, int service, int *msg,  int lenght){


    ServiceHeader p;
    p.header = target; 
    p.payload_size = (CONSTANT_PKT_SIZE-2) + lenght;     // 6 devido ao service header  - 2 flits da noc
    p.service = service;
    p.source = MemoryRead(NI_CONFIG);
    p.target = target;
    p.pkt_size = lenght;

  
  //Waits the DMNI send process be released
  while (MemoryRead(DMNI_SEND_ACTIVE));

    MemoryWrite(DMNI_SIZE, 6);


        
  puts("apo");
  //puts(itoa((unsigned int)p.header));
  //puts("\n");
    //puts(itoh((unsigned int)(&p)));
    //puts("\n");


  MemoryWrite(DMNI_ADDRESS, (unsigned int) (&p) ); //grava na memoria as informacoes do header

    if( lenght > 0 ) {

    MemoryWrite(DMNI_SIZE_2, lenght);
    MemoryWrite(DMNI_ADDRESS_2, (unsigned int) msg);

    }

  MemoryWrite(DMNI_OP, READ);
  MemoryWrite(DMNI_START, 1);
}






void read_packet(ServiceHeader *p){

  MemoryWrite(DMNI_SIZE, 6);
  MemoryWrite(DMNI_OP, WRITE);
  MemoryWrite(DMNI_ADDRESS, 0x4001);
  MemoryWrite(DMNI_START, 1);
  //Waits the DMNI copy all data to memory before release the software to access it
  //while (noc_interruption != 1)
  while (MemoryRead(DMNI_RECEIVE_ACTIVE));

  p = 0x4001;
}





int main()
{
  volatile ServiceHeader p;

  puts("a");

/*
  read_packet((ServiceHeader *)&p);
  puts("$$$ ");
  puts(itoa((unsigned int)p.header));
  puts("\n");
  puts(itoh((unsigned int)p.service));
  puts("\n");
*/


	while(1)
	{
		int i;

    if (noc_interruption){

      i=7;
      //transmite(0x00000101 ,MESSAGE_DELIVERY, &i ,1);

      read_packet((ServiceHeader *)&p);
      //puts("a");
      //puts(itoa((unsigned int)p.header));
      //puts("\n");
      //puts(itoh((unsigned int)p.service));
      //puts("\n");
      


      int data_receive[4];
      data_receive[0] = MemoryRead(0x4001);
      data_receive[1] = MemoryRead(0x4002);
      data_receive[2] = MemoryRead(0x4003);
      data_receive[3] = 4;

      transmite(0x00000101 ,MESSAGE_DELIVERY, data_receive ,4);



    } 

		

	}



}

/*

USO:
int message[30]
transmit( ROUTER_ADDR(X,Y)|MANUAL_<PORT>, message, 30)
*/