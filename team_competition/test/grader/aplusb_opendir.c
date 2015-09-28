/*
TASK: aplusb
LANG: C
*/
// TRIES TO OPENDIR, works ok if unable
#include<stdio.h>

int main(int argc, char ** argv)
{   int a,b;
    if (opendir("..")!=NULL)
        printf("!!!ABLE TO OPENDIR!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
