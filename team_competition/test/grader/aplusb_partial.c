/*
TASK: aplusb
LANG: C
*/
// RIGHT ANSWER BUT TIME LIMIT IF a!=b

#include<stdio.h>

int main()
{   int a,b;
    scanf("%d %d", &a, &b);
    printf("%d\n", a+b);
    if (a!=b)
        while(1);
    return 0;
}
