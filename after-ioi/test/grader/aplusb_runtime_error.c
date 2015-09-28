/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int x(a)
{   return a>-1000 ? 0:1;
}

int main()
{   int a,b;
    scanf("%d %d", &a, &b);
    printf("%d\n", a+b+b/x());
    return 0;
}
