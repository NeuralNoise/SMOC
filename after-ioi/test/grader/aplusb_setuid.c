/*
TASK: aplusb
LANG: C
*/
// TRIES TO SETUID TO 1000, works ok if unable
#include<stdio.h>

int main(int argc, char ** argv)
{   int a,b;
    if (setuid(0)!=-1 || setuid(1000)!=-1)
        printf("!!!ABLE TO SETUID!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
