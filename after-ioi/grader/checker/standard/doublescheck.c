#include<stdio.h>
#include<math.h>

#define EPSILON 0.0000015

int main(int argc, char * argv[])
{   FILE * out = fopen(argv[2],"r");
    FILE * sol = fopen(argv[3],"r");

    double lout,lsol;
    int o, s;

    
    if (out==NULL)
    {   printf("0\nCannot open out file\n");
        return 1;
    }
    if (sol==NULL)
    {   printf("0\nCannot open sol file\n");
        return 1;
    }

    while (1)
    {   o=fscanf(out,"%lf",&lout);
        s=fscanf(sol,"%lf",&lsol);
        if (o!=s)
        {   printf("0\nWrong answer\n");
            return 0;
        }
        if (o<=0 && s<=0)
        {   printf("%s\nAccepted\n", argv[4]);
            return 0;
        }
        if (fabs(lout-lsol)>EPSILON)
        {   printf("0\nWrong answer\n");
            return 0;
        }
    }
}
