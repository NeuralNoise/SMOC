/*
TASK: aplusb
LANG: C
*/
// TRIES TO ECHO, works ok if unable
#include<stdio.h>

int main(int argc, char ** argv)
{   int a,b;
    if (system("echo x")!=-1)
        printf("!!!ABLE TO USE CMD!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
