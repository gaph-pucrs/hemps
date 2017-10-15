

#define MemoryWrite(A,V) *(volatile unsigned int*)(A)=(V)
#define MemoryRead(A) (*(volatile unsigned int*)(A))

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


typedef struct {
	unsigned int header;				
	unsigned int payload_size;			
	unsigned int service;				
	unsigned int source;				
	unsigned int target;
	unsigned int pkt_size;				
} ServiceHeader;


void transmite(int target, int service, int *msg,  int lenght){


    ServiceHeader *p;
	p -> header = target;
	p -> payload_size = 6 + lenght;     // 6 devido ao service header 
	p -> service = service;
    p -> source = MemoryRead(NI_CONFIG);
    p -> target = target;
    p -> pkt_size = lenght;


	//Waits the DMNI send process be released
	while (MemoryRead(DMNI_SEND_ACTIVE));

  	MemoryWrite(DMNI_SIZE, 6);
	MemoryWrite(DMNI_ADDRESS, (unsigned int)p ); //grava na memoria as informacoes do header

    if( lenght > 0 ) {

		MemoryWrite(DMNI_SIZE_2, lenght);
		MemoryWrite(DMNI_ADDRESS_2, msg);

    }

	MemoryWrite(DMNI_OP, READ);
	MemoryWrite(DMNI_START, 1);
}


int main()
{
    ServiceHeader *p;
	p -> header = 1;
	p -> payload_size = 2;     // 6 devido ao service header 
	p -> service = 3;
    p -> source = 4;
    p -> target = 5;
    p -> pkt_size = 6;
	MemoryWrite(DMNI_SIZE, 6);
	MemoryWrite(DMNI_ADDRESS, (unsigned int)p ); //grava na memoria as informacoes do header


	unsigned int data_dma_code[102];
	data_dma_code[0]= 0x241d7fff;
	data_dma_code[1]= 0x0c00002c;
	data_dma_code[2]= 0x00000000;
	data_dma_code[3]= 0x00002021;
	data_dma_code[4]= 0x0000000c;
	data_dma_code[5]= 0x00000000;
	data_dma_code[6]= 0x08000006;
	data_dma_code[7]= 0x00000000;
	data_dma_code[8]= 0x0000000c;
	data_dma_code[9]= 0x00000000;
	data_dma_code[10]= 0x03e00008;
	data_dma_code[11]= 0x00000000;
	data_dma_code[12]= 0x24e20006;
	data_dma_code[13]= 0xac450008;
	data_dma_code[14]= 0xac440000;
	data_dma_code[15]= 0xac420004;
	data_dma_code[16]= 0x3c022000;
	data_dma_code[17]= 0x34420140;
	data_dma_code[18]= 0x8c430000;
	data_dma_code[19]= 0x3c052000;
	data_dma_code[20]= 0xac43000c;
	data_dma_code[21]= 0xac440010;
	data_dma_code[22]= 0xac470014;
	data_dma_code[23]= 0x34a30250;
	data_dma_code[24]= 0x8c620000;
	data_dma_code[25]= 0x00000000;
	data_dma_code[26]= 0x1440fffd;
	data_dma_code[27]= 0x34a20200;
	data_dma_code[28]= 0x34a40210;
	data_dma_code[29]= 0x24030006;
	data_dma_code[30]= 0xac430000;
	data_dma_code[31]= 0xac820000;
	data_dma_code[32]= 0x18e00004;
	data_dma_code[33]= 0x34a20205;
	data_dma_code[34]= 0x34a30215;
	data_dma_code[35]= 0xac470000;
	data_dma_code[36]= 0xac660000;
	data_dma_code[37]= 0x34a20230;
	data_dma_code[38]= 0x34a30220;
	data_dma_code[39]= 0x24040001;
	data_dma_code[40]= 0xac600000;
	data_dma_code[41]= 0xac440000;
	data_dma_code[42]= 0x03e00008;
	data_dma_code[43]= 0x00000000;
	data_dma_code[44]= 0x27bdffa8;
	data_dma_code[45]= 0x24020001;
	data_dma_code[46]= 0x24030002;
	data_dma_code[47]= 0xafa20010;
	data_dma_code[48]= 0xafa30014;
	data_dma_code[49]= 0x24020003;
	data_dma_code[50]= 0x24030004;
	data_dma_code[51]= 0xafa20018;
	data_dma_code[52]= 0xafa3001c;
	data_dma_code[53]= 0x24020005;
	data_dma_code[54]= 0x24030006;
	data_dma_code[55]= 0xafa20020;
	data_dma_code[56]= 0xafa30024;
	data_dma_code[57]= 0x24020007;
	data_dma_code[58]= 0x24030008;
	data_dma_code[59]= 0xafa20028;
	data_dma_code[60]= 0xafa3002c;
	data_dma_code[61]= 0x24020009;
	data_dma_code[62]= 0x2403000a;
	data_dma_code[63]= 0xafa20030;
	data_dma_code[64]= 0xafa30034;
	data_dma_code[65]= 0x2402000b;
	data_dma_code[66]= 0x2403000c;
	data_dma_code[67]= 0xafb00050;
	data_dma_code[68]= 0xafa20038;
	data_dma_code[69]= 0x27b00010;
	data_dma_code[70]= 0xafa3003c;
	data_dma_code[71]= 0x2402000d;
	data_dma_code[72]= 0x2403000e;
	data_dma_code[73]= 0x24040001;
	data_dma_code[74]= 0x24050290;
	data_dma_code[75]= 0x02003021;
	data_dma_code[76]= 0x24070010;
	data_dma_code[77]= 0xafa20040;
	data_dma_code[78]= 0xafa30044;
	data_dma_code[79]= 0x2402000f;
	data_dma_code[80]= 0x24030010;
	data_dma_code[81]= 0xafbf0054;
	data_dma_code[82]= 0xafa20048;
	data_dma_code[83]= 0x0c00000c;
	data_dma_code[84]= 0xafa3004c;
	data_dma_code[85]= 0x24040100;
	data_dma_code[86]= 0x24050290;
	data_dma_code[87]= 0x02003021;
	data_dma_code[88]= 0x0c00000c;
	data_dma_code[89]= 0x24070010;
	data_dma_code[90]= 0x00002021;
	data_dma_code[91]= 0x24050290;
	data_dma_code[92]= 0x02003021;
	data_dma_code[93]= 0x0c00000c;
	data_dma_code[94]= 0x24070010;
	data_dma_code[95]= 0x02003021;
	data_dma_code[96]= 0x24040101;
	data_dma_code[97]= 0x24050290;
	data_dma_code[98]= 0x0c00000c;
	data_dma_code[99]= 0x24070010;
	data_dma_code[100]= 0x08000064;
	data_dma_code[101]= 0x00000000;

	transmite(0x00000001 ,0x00000290, data_dma_code ,102);
	transmite(0x00000100 ,0x00000290, data_dma_code ,102);
	transmite(0x00000000 ,0x00000290, data_dma_code ,102);
	transmite(0x00000101 ,0x00000290, data_dma_code,102);


	while(1);



}

/*

USO:
int message[30]
transmit( ROUTER_ADDR(X,Y)|MANUAL_<PORT>, message, 30)
*/