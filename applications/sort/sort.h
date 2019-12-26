#include <libos.h>

#define ARRAY_SIZE 40
#define LFSR_POL 0xA3000000

#define TASK_REQUEST    0x1000
#define KILL_PROC		    0x2000
#define MSG_ACK			    0x3000

char *itoasc(unsigned int num)
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

unsigned int rand_val = LFSR_POL;

unsigned int rand(){
	rand_val = (rand_val >> 1) ^ (-(rand_val & 1u) & LFSR_POL);
	return rand_val;
}

void swap(int *array, int a, int b){
	int aux;

	aux = array[a];
	array[a] = array[b];
	array[b] = aux;
}

int partition(int *array, int lo, int hi){
	int pivo, i, j;
	int index;

	i = lo - 1;
	index = rand() % (hi - lo) + lo;
	swap(array, index, hi);
	pivo = array[hi];

	for (j = lo; j < hi; j++){
		if (array[j] < pivo){
			i++;
			swap(array, i, j);
		}
	}
	if (array[hi] < array[i+1])
		swap(array, i+1, hi);
	return i+1;
}

void quicksort(int *array, int lo, int hi) {
	int p;
    if (lo < hi){
        p = partition(array, lo, hi);
        quicksort(array, lo, p - 1);
        quicksort(array, p + 1, hi);
    }
}
