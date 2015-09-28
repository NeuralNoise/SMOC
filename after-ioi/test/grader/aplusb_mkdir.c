/*
TASK: aplusb
LANG: C
*/
// TRIES TO MKDIR, works ok if unable
#include<stdio.h>

int main(int argc, char ** argv)
{   int a,b;
    if (mkdir("test")!=-1 || mkdir("/tmp/test")!=-1)
        printf("!!!ABLE TO MKDIR!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
