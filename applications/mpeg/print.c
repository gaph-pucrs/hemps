#include <libos.h>
#include "map_pkg.h"
#include "mpeg_std.h"

typedef int type_DATA; //unsigned



int main()
{
    printf("MPEG Task PRINT start:\n");
    int i;
    int *buff;
    int size;
    int src;



    for(i=0;i<MPEG_FRAMES;i++)
    {
        prepare_recv_msg(&src, &size);
        buff = wait_receive();
        printf("Finished\n");

    }
    free(buff);
    printf("End Task E - MPEG\n");

    exit();
}
