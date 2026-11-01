#include <cstdio>
using namespace std;

typedef long long ll;

static char buf[1 << 25];

int main() {
    freopen("huffman.in", "r", stdin);
    freopen("huffman.out", "w", stdout);

    size_t len = fread(buf, 1, sizeof(buf), stdin);
    buf[len] = 0;
    char *p = buf;

    auto readInt = [&]() -> ll {
        while (*p && (*p < '0' || *p > '9')) p++;
        ll x = 0;
        while (*p >= '0' && *p <= '9') x = x * 10 + (*p++ - '0');
        return x;
    };

    int n = (int)readInt();
    ll *a = new ll[n];
    ll *b = new ll[n];   
    for (int i = 0; i < n; i++) a[i] = readInt();

    int ia = 0, ib = 0, eb = 0;   
    ll total = 0;

    auto take = [&]() -> ll {
        if (ia < n && (ib >= eb || a[ia] <= b[ib])) return a[ia++];
        return b[ib++];
    };

    for (int i = 0; i < n - 1; i++) {
        ll x = take();
        ll y = take();
        ll s = x + y;
        total += s;
        b[eb++] = s;
    }

    printf("%lld\n", total);

    delete[] a;
    delete[] b;
    return 0;
}