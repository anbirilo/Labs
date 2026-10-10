#include <iostream>
#include <vector>
#include <algorithm>
#include <fstream>

int main() {
    std::ifstream fin("input.txt");
    std::ofstream fout("output.txt");

    int n{};
    fin >> n;

    std::vector<int> A(n + 1);
    std::vector<int> tails;

    for (size_t i = 1; i < n + 1; i++) {
        fin >> A[i];
    }

    for (size_t i = 1; i < n + 1; i++) {
        auto it = std::lower_bound(tails.begin(), tails.end(), A[i]);
        if (it == tails.end()) {
            tails.push_back(A[i]);
        } else {
            *it = A[i];
        }
    }

    fout << tails.size() << std::endl;

    return 0;
}