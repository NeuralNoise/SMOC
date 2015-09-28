/*
TASK: mod
LANG: C
*/

#include<stdio.h>

int main()
{   int s,u,n,r;
    scanf("%d %d", &s, &u);
    while(1)
    {   n = (s+u)/2;
        printf("%d\n", n);
        scanf("%d", &r);
        if (r<0)
            u = n-1;
        else if (r>0)
            s = n+1;
        if (r==0)
            break;
    } 

    return 0;
}
