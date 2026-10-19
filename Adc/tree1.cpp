#include <fstream>
#include <stack>

const int MAXN = 1000000; 

struct Node {
    long long key;
    int left = -1;
    int right = -1;
};

Node tree[MAXN];
int n = 0;        

int main() {
    std::ifstream in("input.txt");
    std::ofstream out("output.txt");

    long long x;

    while (in >> x) {
        if (n == 0) {
            tree[n++].key = x;
            continue;
        }
        int cur = 0;
        while (true) {
            if (x == tree[cur].key) break;          
            if (x < tree[cur].key) {
                if (tree[cur].left == -1) {
                    tree[n].key = x;
                    tree[cur].left = n++;
                    break;
                }
                cur = tree[cur].left;
            } else {
                if (tree[cur].right == -1) {
                    tree[n].key = x;
                    tree[cur].right = n++;
                    break;
                }
                cur = tree[cur].right;
            }
        }
    }

    if (n == 0) return 0;

    std::stack<int> st;
    st.push(0);
    while (!st.empty()) {
        int v = st.top();
        st.pop();
        out << tree[v].key << "\n";
        if (tree[v].right != -1) st.push(tree[v].right);
        if (tree[v].left != -1) st.push(tree[v].left);
    }
    return 0;
}