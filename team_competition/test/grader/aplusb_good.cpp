/*
TASK: aplusb
LANG: C++
*/
#include<iostream>
#include<vector>

using namespace std;

int main()
{   int a;
    vector<int> v;
    cin >> a;
    v.push_back(a);
    cin >> a;
    v.push_back(a);
    cout << v[0]+v[1] << endl;
    return 0;
}
