/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int deep(int x)
{   if (x>0)
        return ((deep(x-1))&1)<<1;
    else
        return 1;
}

int main()
{   int a,b,x;
    x = deep(1024*1024);
    if (x>0)
        printf("!!!STACK STANGING!!!\n");
    scanf("%d %d", &a, &b);
    printf("%d\n", a+b);
    return 0;
}
