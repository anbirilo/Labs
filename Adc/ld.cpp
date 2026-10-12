#include <fstream>
#include <string>
#include <vector>
#include <algorithm>
using namespace std;

int main() {
    ifstream fin("in.txt");
    ofstream fout("out.txt");

    long long pD, pI, pR;       
    string a, b;                
    fin >> pD >> pI >> pR >> a >> b;

    int n = a.size();
    int m = b.size();

    vector<vector<long long>> f(n + 1, vector<long long>(m + 1, 0));

    for (int i = 0; i <= n; i++) f[i][0] = i * pD;   
    for (int j = 0; j <= m; j++) f[0][j] = j * pI;   

    for (int i = 1; i <= n; i++) {
        for (int j = 1; j <= m; j++) {
            long long del = f[i - 1][j] + pD;                         
            long long ins = f[i][j - 1] + pI;                         
            long long rep = f[i - 1][j - 1] + (a[i - 1] != b[j - 1] ? pR : 0);
            f[i][j] = min({del, ins, rep});
        }
    }

    fout << f[n][m];
    return 0;
}