#include <cstdio>
#include <utility>
#include <string>
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

    int n, q;
    scanf("%d %d", &n, &q);

    parent_ = new int[n + 1];
    sz_ = new int[n + 1];
    for (int i = 1; i <= n; i++) {
        parent_[i] = i;
        sz_[i] = 1;
    }

    int components = n;
    string out;
    out.reserve(q * 7);

    for (int i = 0; i < q; i++) {
        int u, v;
        scanf("%d %d", &u, &v);
        if (unite(u, v)) components--;
        out += to_string(components);
        out += '\n';
    }

    fputs(out.c_str(), stdout);

    delete[] parent_;
    delete[] sz_;
    return 0;
}