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
int root = -1;   

int main() {
    std::ifstream in("input.txt");
    std::ofstream out("output.txt");

    long long del, x;
    in >> del;

    while (in >> x) {
        if (root == -1) {
            tree[n].key = x;
            root = n++;
            continue;
        }
        int cur = root;
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

    int cur = root, parent = -1;
    while (cur != -1 && tree[cur].key != del) {
        parent = cur;
        cur = (del < tree[cur].key) ? tree[cur].left : tree[cur].right;
    }

    if (cur != -1) {   
        if (tree[cur].left != -1 && tree[cur].right != -1) {
            int mp = cur;                
            int m = tree[cur].right;      
            while (tree[m].left != -1) {  
                mp = m;
                m = tree[m].left;
            }
            tree[cur].key = tree[m].key;
            if (mp == cur) tree[mp].right = tree[m].right;
            else           tree[mp].left  = tree[m].right;
        } else {
            int child = (tree[cur].left != -1) ? tree[cur].left : tree[cur].right;
            if (parent == -1)                root = child;
            else if (tree[parent].left == cur) tree[parent].left = child;
            else                             tree[parent].right = child;
        }
    }

    if (root == -1) return 0;

    std::stack<int> st;
    st.push(root);
    while (!st.empty()) {
        int v = st.top();
        st.pop();
        out << tree[v].key << "\n";
        if (tree[v].right != -1) st.push(tree[v].right);
        if (tree[v].left != -1) st.push(tree[v].left);
    }
    return 0;
}