#include <cstdio>

const int MAXV = 750000 + 5;

static FILE* in;
static char buf[1 << 16];
static size_t bufLen = 0, bufPos = 0;

static inline int readChar() {
    if (bufPos == bufLen) {
        bufLen = fread(buf, 1, sizeof(buf), in);
        bufPos = 0;
        if (bufLen == 0) return EOF;
    }
    return buf[bufPos++];
}

static inline int readInt() {
    int c = readChar();
    while (c != EOF && (c < '0' || c > '9')) c = readChar();
    int x = 0;
    while (c >= '0' && c <= '9') {
        x = x * 10 + (c - '0');
        c = readChar();
    }
    return x;
}

static int parent[MAXV];
static int components;

static inline int findRoot(int x) {
    while (parent[x] >= 0) {
        int p = parent[x];
        if (parent[p] >= 0) parent[x] = parent[p]; 
        x = parent[x];
    }
    return x;
}

static inline void unite(int a, int b) {
    a = findRoot(a);
    b = findRoot(b);
    if (a == b) return;
    if (parent[a] > parent[b]) { 
        int t = a;
        a = b;
        b = t;
    }
    parent[a] += parent[b];
    parent[b] = a;
    --components;
}

static int eu[MAXV], ev[MAXV];
static int query[MAXV];
static bool removed[MAXV];
static char answer[MAXV];

int main() {
    in = fopen("input.txt", "rb");
    if (!in) return 1;

    int n = readInt(), m = readInt(), q = readInt();

    for (int i = 1; i <= m; ++i) {
        eu[i] = readInt();
        ev[i] = readInt();
    }
    for (int i = 0; i < q; ++i) {
        query[i] = readInt();
        removed[query[i]] = true;
    }
    fclose(in);

    for (int v = 1; v <= n; ++v) parent[v] = -1;
    components = n;

    for (int i = 1; i <= m; ++i) {
        if (!removed[i]) unite(eu[i], ev[i]);
    }

    for (int i = q - 1; i >= 0; --i) {
        answer[i] = (components == 1) ? '1' : '0';
        unite(eu[query[i]], ev[query[i]]);
    }

    FILE* out = fopen("output.txt", "wb");
    if (!out) return 1;
    fwrite(answer, 1, q, out);
    fputc('\n', out);
    fclose(out);
    return 0;
}