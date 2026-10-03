#include <iostream>
#include <vector>

int main() {
    long long n, k{};
    std::cin >> n >> k;
        
    const long long MOD = 1000000007;

    std::vector<std::vector<long long>> F(n + 1, std::vector<long long>(n + 1, 0));
    
    F[0][0] = 1; 
    
    for (size_t i = 1; i <= n; i++) {
        F[i][0] = 1;
        F[i][i] = 1;
    }
    
    for (size_t i = 1; i <= n; i++) {
        for (size_t j = 1; j <= i - 1; j++) {
            F[i][j] = (F[i - 1][j - 1] + F[i - 1][j]) % MOD;
        }
    }
    
    std::cout << F[n][k];
    
    return 0;
}
