/*
TASK: aplusb
LANG: C
*/
// TRIES TO ACCESS SELF OR CREATE FILES, works ok if unable
#include<stdio.h>
#include <pthread.h>

void * do_nothing(void * x)
{   return x;
}

int main(int argc, char ** argv)
{   int a,b;
    pthread_t thread;
    if (pthread_create(&thread, NULL, do_nothing, NULL)>=0)
        printf("!!!ABLE TO USE THREADING!!!\n");
    else
    {   scanf("%d %d", &a, &b);
        printf("%d\n", a+b);
    }
    return 0;
}
