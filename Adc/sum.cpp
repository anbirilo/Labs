
#include <iostream>
#include <string>
using namespace std;

long long* bit;
int n;

void Add(int i, long long x) {
    i++;

    while (i <= n) {
        bit[i] += x;
        i += i & (-i);
    }
}

long long PrefixSum(int i) {
    long long sum = 0;

    while (i > 0) {
        sum += bit[i];
        i -= i & (-i);
    }

    return sum;
}

long long FindSum(int l, int r) {
    return PrefixSum(r) - PrefixSum(l);
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    cin >> n;

    bit = new long long[n + 1]();

    for (int i = 0; i < n; i++) {
        long long x;
        cin >> x;
        Add(i, x);
    }

    int q;
    cin >> q;

    for (int j = 0; j < q; j++) {
        string command;
        cin >> command;

        if (command == "Add") {
            int i;
            long long x;
            cin >> i >> x;

            Add(i, x);
        }
        else if (command == "FindSum") {
            int l, r;
            cin >> l >> r;

            cout << FindSum(l, r) << '\n';
        }
    }

    delete[] bit;

    return 0;
}
