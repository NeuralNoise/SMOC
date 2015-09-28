/*
TASK: aplusb
LANG: C
*/
#include<stdio.h>

int x[10];

int main()
{   int a,b;
    scanf("%d %d", &a, &b);
    x[100000] = a;
    printf("%d\n", b+x[100000]);
    return 0;
}
