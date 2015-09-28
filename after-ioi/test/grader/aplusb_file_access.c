/*
TASK: aplusb
LANG: C
*/
// TRIES TO ACCESS SELF OR CREATE FILES, works ok if unable
#include<stdio.h>

int main(int argc, char ** argv)
{   int a,b;
    if (open(argv[0], 0)!=-1 || 
        open("test.txt", 1)!=-1 || 
        open("/tmp/test.txt", 1)!=-1)
        printf("!!!ABLE TO ACCESS FILES!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
