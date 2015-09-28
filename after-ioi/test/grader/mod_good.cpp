/*
TASK: mod
LANG: C++
*/

#include<iostream>

using namespace std;

int main()
{   int s,u,n,r;
    cin >> s >> u;
    while(1)
    {   n = (s+u)/2;
        cout << n << endl;
        cin >> r;
        if (r<0)
            u = n-1;
        else if (r>0)
            s = n+1;
        if (r==0)
            break;
    } 

    return 0;
}
