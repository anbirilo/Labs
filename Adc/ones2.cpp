#include <iostream>

const long long MOD = 1000000007;

long long power(long long a, long long b) {
    long long result = 1;
    a %= MOD;
    while (b > 0) {
        if (b & 1) result = result * a % MOD;
        a = a * a % MOD;
        b >>= 1;
    }
    return result;
}

int main() {
    long long n, k;
    std::cin >> n >> k;

    long long* fact = new long long[n + 1];
    fact[0] = 1;
    for (long long i = 1; i <= n; i++) {
        fact[i] = fact[i - 1] * i % MOD;
    }

    long long ans = fact[n]
                  * power(fact[k], MOD - 2) % MOD
                  * power(fact[n - k], MOD - 2) % MOD;

    std::cout << ans;

    delete[] fact;

    return 0;
}