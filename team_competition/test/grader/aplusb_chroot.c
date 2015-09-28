/*
TASK: aplusb
LANG: C
*/
// TRIES TO READ /etc/passwd, works ok if unable
#include<stdio.h>
#include<unistd.h>

int main(int argc, char ** argv)
{   int a,b;
    if (chroot("/etc/")!=-1)
        printf("!!!NOT CHROOT!!!");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
