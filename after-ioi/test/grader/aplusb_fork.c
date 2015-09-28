/*
TASK: aplusb
LANG: C
*/
// FORKS - wa if test is not OK
#include<stdio.h>

int main()
{   int a,b;
    if (fork()==-1)
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    else 
        printf("!!!ABLE TO FORK!!!\n");
    return 0;
}
