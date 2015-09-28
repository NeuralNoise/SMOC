/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int * x;

int main()
{   int a,b;
    x = malloc(16*1024*1024*sizeof(int));
    if (x)
        printf("!!!ABLE TO MALLOC!!!\n");
    else
    {   scanf("%d %d", x, x+1000);
        printf("%d\n", *x+*(x+1000));
    }
    return 0;
}
