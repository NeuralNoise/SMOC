/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int x[16*1024*1024];

int main()
{   int a,b;
    scanf("%d %d", &a, &b);
    x[1024*1024] = a;
    printf("%d\n", x[1024*1024]+b);
    return 0;
}
