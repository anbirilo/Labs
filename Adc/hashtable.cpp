#include <cstdio>

const int MAXM = 10000;

int main() {
    FILE* in = fopen("input.txt", "r");
    FILE* out = fopen("output.txt", "w");
    if (!in || !out) return 1;

    int m, c, n;
    if (fscanf(in, "%d %d %d", &m, &c, &n) != 3) return 1;

    static int table[MAXM];
    for (int i = 0; i < m; ++i) table[i] = -1;  

    for (int k = 0; k < n; ++k) {
        int x;
        if (fscanf(in, "%d", &x) != 1) return 1;

        int pos = x % m;  
        while (table[pos] != -1 && table[pos] != x) {
            pos += c; 
            if (pos >= m) pos -= m;  
        }
        table[pos] = x;  
    }

    for (int i = 0; i < m; ++i) {
        fprintf(out, i ? " %d" : "%d", table[i]);
    }
    fprintf(out, "\n");

    fclose(in);
    fclose(out);
    return 0;
}