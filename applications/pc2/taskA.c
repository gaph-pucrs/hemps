#include <api.h>
#include <stdlib.h>
Message msg;
int main(){
 int i;
 Echo("task B started.");
 Echo(itoa(GetTick()));
 for(i=0;i<500;i++){
 Send(&msg,taskB);
 }
 Echo(itoa(GetTick()));
 Echo("task A finished.");
 exit();
}