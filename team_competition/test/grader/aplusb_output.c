/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int main()
{   int a,b,i;
    scanf("%d %d", &a, &b);
    for (i=0;i<1024*1024;++i)
        printf("%d\n", a+b);
    return 0;
}
