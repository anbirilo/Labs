#include <iostream>
#include <vector>
#include <fstream>
#include <algorithm>
#include <climits>

long long MatrixMultiplicationOrder(const std::vector<long long>& dim) {
    int n = dim.size();
    std::vector<std::vector<long long>> m(n, std::vector<long long>(n, 0));

    for (int l = 2; l < n; l++) {
        for (int i = 1; i < n - l + 1; i++) {
            int j = i + l - 1;
            m[i][j] = LLONG_MAX;
            for (int k = i; k < j; k++) {
                long long cost = m[i][k] + m[k + 1][j] + dim[i - 1] * dim[k] * dim[j];
                if (cost < m[i][j]) {
                    m[i][j] = cost;
                }
            }
        }
    }

    return m[1][n - 1];
}

int main() {
    std::ifstream fin("input.txt");
    std::ofstream fout("output.txt");

    int s;
    fin >> s;

    std::vector<long long> dim(s + 1);
    for (int i = 1; i <= s; i++) {
        long long n, m;
        fin >> n >> m;
        if (i == 1) dim[0] = n;
        dim[i] = m;
    }

    fout << MatrixMultiplicationOrder(dim);
}