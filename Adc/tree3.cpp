#include <fstream>
#include <algorithm>

const int MAXN = 800001;
const long long INF = (long long)1e18;

long long key[MAXN], lo[MAXN], hi[MAXN];

int main() {
    std::ifstream in("bst.in");
    std::ofstream out("bst.out");

    int n;
    in >> n;
    in >> key[1];
    lo[1] = -INF;
    hi[1] = INF;

    bool ok = true;
    for (int i = 2; i <= n; i++) {
        int p;
        char c;
        in >> key[i] >> p >> c;

        if (c == 'L') {
            lo[i] = lo[p];
            hi[i] = std::min(hi[p], key[p]);   
        } else {
            lo[i] = std::max(lo[p], key[p]);   
            hi[i] = hi[p];
        }

        if (!(lo[i] <= key[i] && key[i] < hi[i])) {
            ok = false;
            break;
        }
    }

    out << (ok ? "YES" : "NO") << "\n";
    return 0;
}