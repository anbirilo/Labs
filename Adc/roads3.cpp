
#include <iostream>
using namespace std;

int* parent;
int* sz;
int components;

int findSet(int v) {
    int root = v;

    while (parent[root] != root) {
        root = parent[root];
    }

    while (parent[v] != v) {
        int p = parent[v];
        parent[v] = root;
        v = p;
    }

    return root;
}

void unionSet(int a, int b) {
    a = findSet(a);
    b = findSet(b);

    if (a != b) {
        if (sz[a] < sz[b]) {
            int temp = a;
            a = b;
            b = temp;
        }

        parent[b] = a;
        sz[a] += sz[b];

        components--;
    }
}

void initDSU(int n) {
    parent = new int[n + 1];
    sz = new int[n + 1];

    components = n;

    for (int i = 1; i <= n; i++) {
        parent[i] = i;
        sz[i] = 1;
    }
}

bool isConnected() {
    return components == 1;
}

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    int n, m, q;
    cin >> n >> m >> q;

    int* u = new int[m + 1];
    int* v = new int[m + 1];

    int* x = new int[q];

    bool* destroyed = new bool[m + 1]();

    char* ans = new char[q];

    for (int i = 1; i <= m; i++) {
        cin >> u[i] >> v[i];
    }

    for (int i = 0; i < q; i++) {
        cin >> x[i];
        destroyed[x[i]] = true;
    }

    initDSU(n);

    for (int i = 1; i <= m; i++) {
        if (!destroyed[i]) {
            unionSet(u[i], v[i]);
        }
    }

    for (int i = q - 1; i >= 0; i--) {

        if (isConnected()) {
            ans[i] = '1';
        } else {
            ans[i] = '0';
        }

        int id = x[i];
        unionSet(u[id], v[id]);
    }

    for (int i = 0; i < q; i++) {
        cout << ans[i];
    }

    cout << '\n';

    delete[] u;
    delete[] v;
    delete[] x;
    delete[] destroyed;
    delete[] ans;
    delete[] parent;
    delete[] sz;

    return 0;
}
