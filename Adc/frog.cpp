#include <iostream>
#include <vector>
#include <climits>
#include <algorithm>

int main() {
    int n{};
    std::cin >> n;
    std::vector<int> array(n + 1);
    for (int i = 1; i <= n; i++)
    {
        std::cin >> array.at(i);
    }
    
    std::vector<int> F(n + 1, INT_MIN);
    std::vector<bool> jumpBy2(n + 1, false);
    F[1] = array[1];
    
    for (int i = 1; i <= n; i++)
    {
        if (F[i] == INT_MIN) continue;
        if (i + 2 <= n) {
            long long candidate = F[i] + array[i + 2];
            if (candidate > F[i + 2]) {
                F[i + 2] = candidate;
                jumpBy2[i + 2] = true;
            }
        }
        if (i + 3 <= n) {
            long long candidate = F[i] + array[i + 3];
            if (candidate > F[i + 3]) {
                F[i + 3] = candidate;
                jumpBy2[i + 3] = false;
            }
        }
    }
    
    if (F[n] == INT_MIN) {
        std::cout << -1 << "\n";
        return 0;
    }
    
    std::vector<int> path;
    int cur = n;
    while (cur != 1) {
        path.push_back(cur);
        cur = jumpBy2[cur] ? cur - 2 : cur - 3;
    }
    path.push_back(1);
    std::reverse(path.begin(), path.end());
    
    std::cout << F[n] << "\n";
    for (int idx : path) {
        std::cout << idx << " ";
    }
    std::cout << "\n";
    
    return 0;
}