#include <cstdio>
#include <utility>
using namespace std;

int* parent_ = nullptr;
int* sz_ = nullptr;

int find(int x) {
    int root = x;
    while (parent_[root] != root) root = parent_[root];
    while (parent_[x] != root) {
        int next = parent_[x];
        parent_[x] = root;
        x = next;
    }
    return root;
}

bool unite(int a, int b) {
    a = find(a);
    b = find(b);
    if (a == b) return false;
    if (sz_[a] < sz_[b]) swap(a, b);
    parent_[b] = a;
    sz_[a] += sz_[b];
    return true;
}

int main() {
    freopen("input.txt", "r", stdin);
    freopen("output.txt", "w", stdout);

    int n, m, q;
    scanf("%d %d %d", &n, &m, &q);

    int* eu = new int[m + 1];
    int* ev = new int[m + 1];
    for (int i = 1; i <= m; i++) scanf("%d %d", &eu[i], &ev[i]);

    int* x = new int[q + 1];
    bool* destroyed = new bool[m + 1];
    for (int i = 1; i <= m; i++) destroyed[i] = false;
    for (int i = 1; i <= q; i++) {
        scanf("%d", &x[i]);
        destroyed[x[i]] = true;
    }

    parent_ = new int[n + 1];
    sz_ = new int[n + 1];
    for (int i = 1; i <= n; i++) {
        parent_[i] = i;
        sz_[i] = 1;
    }

    int components = n;
    for (int i = 1; i <= m; i++) {
        if (!destroyed[i] && unite(eu[i], ev[i])) components--;
    }

    char* ans = new char[q + 2];
    ans[q] = '\0';
    for (int i = q; i >= 1; i--) {
        ans[i - 1] = (components == 1) ? '1' : '0';
        if (unite(eu[x[i]], ev[x[i]])) components--;
    }

    puts(ans);

    delete[] eu;
    delete[] ev;
    delete[] x;
    delete[] destroyed;
    delete[] parent_;
    delete[] sz_;
    delete[] ans;
    return 0;
}