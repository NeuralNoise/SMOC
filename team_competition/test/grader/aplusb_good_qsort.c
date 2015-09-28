/*
TASK: aplusb
LANG: C
*/
// performs qsort - expects input < 10
#include<stdio.h>

#define MAXN 40000

int c[MAXN];

int cmp(const void * v1, const void * v2)
{   return *(int *)v1 - *(int *)v2;
}

int main()
{   int i;
    for (i=0;i<MAXN ;++i)
        c[i]=i+100;
    scanf("%d %d", &c[1000], &c[2000]);
    qsort(c, MAXN, sizeof(int), cmp);
    printf("%d\n", c[0]+c[1]);
    return 0;
}
