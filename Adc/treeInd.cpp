
#include <iostream>
#include <fstream>
#include <climits>

using namespace std;

struct Node
{
    int key;
    int left;
    int right;
    int parent;

    int dist[2];
    long long sum[2];
    int leaf[2];
};

void Insert(Node* tree, int& count, int key, int& root)
{
    if (root == -1)
    {
        tree[count].key = key;
        tree[count].left = -1;
        tree[count].right = -1;
        tree[count].parent = -1;

        root = count;
        count++;

        return;
    }

    int current = root;

    while (true)
    {
        if (key == tree[current].key)
            return;

        if (key < tree[current].key)
        {
            if (tree[current].left == -1)
            {
                tree[count].key = key;
                tree[count].left = -1;
                tree[count].right = -1;
                tree[count].parent = current;

                tree[current].left = count;
                count++;

                return;
            }

            current = tree[current].left;
        }
        else
        {
            if (tree[current].right == -1)
            {
                tree[count].key = key;
                tree[count].left = -1;
                tree[count].right = -1;
                tree[count].parent = current;

                tree[current].right = count;
                count++;

                return;
            }

            current = tree[current].right;
        }
    }
}

void Update(Node* tree, int v, int parity, int dist, long long sum, int leaf)
{
    if (tree[v].dist[parity] == -1 ||
        dist < tree[v].dist[parity] ||
        (dist == tree[v].dist[parity] &&
         (sum < tree[v].sum[parity] ||
          (sum == tree[v].sum[parity] &&
           leaf < tree[v].leaf[parity]))))
    {
        tree[v].dist[parity] = dist;
        tree[v].sum[parity] = sum;
        tree[v].leaf[parity] = leaf;
    }
}

void FindPaths(Node* tree, int v,
               int& bestLength,
               long long& bestSum,
               int& bestRoot,
               int& bestLeft,
               int& bestRight)
{
    if (v == -1)
        return;

    int l = tree[v].left;
    int r = tree[v].right;

    if (l != -1)
        FindPaths(tree, l, bestLength, bestSum,
                  bestRoot, bestLeft, bestRight);

    if (r != -1)
        FindPaths(tree, r, bestLength, bestSum,
                  bestRoot, bestLeft, bestRight);

    if (l == -1 && r == -1)
    {
        tree[v].dist[0] = 0;
        tree[v].sum[0] = tree[v].key;
        tree[v].leaf[0] = v;

        return;
    }

    for (int i = 0; i < 2; i++)
    {
        if (l != -1 && tree[l].dist[i] != -1)
        {
            Update(tree, v, 1 - i,
                   tree[l].dist[i] + 1,
                   tree[l].sum[i] + tree[v].key,
                   tree[l].leaf[i]);
        }

        if (r != -1 && tree[r].dist[i] != -1)
        {
            Update(tree, v, 1 - i,
                   tree[r].dist[i] + 1,
                   tree[r].sum[i] + tree[v].key,
                   tree[r].leaf[i]);
        }
    }

    if (l != -1 && r != -1)
    {
        for (int i = 0; i < 2; i++)
        {
            if (tree[l].dist[i] == -1 ||
                tree[r].dist[i] == -1)
                continue;

            int length =
                tree[l].dist[i] + tree[r].dist[i] + 2;

            long long sum =
                tree[l].sum[i] + tree[r].sum[i] + tree[v].key;

            if (length < bestLength ||
                (length == bestLength && sum < bestSum) ||
                (length == bestLength && sum == bestSum &&
                 (bestRoot == -1 ||
                  tree[v].key < tree[bestRoot].key)))
            {
                bestLength = length;
                bestSum = sum;
                bestRoot = v;

                bestLeft = tree[l].leaf[i];
                bestRight = tree[r].leaf[i];
            }
        }
    }
}

int FindMinimum(Node* tree, int v)
{
    while (tree[v].left != -1)
        v = tree[v].left;

    return v;
}

void DeleteRight(Node* tree, int& root, int v)
{
    int child;

    if (tree[v].left != -1 && tree[v].right != -1)
    {
        int successor = FindMinimum(tree, tree[v].right);

        tree[v].key = tree[successor].key;

        v = successor;
    }

    if (tree[v].left != -1)
        child = tree[v].left;
    else
        child = tree[v].right;

    int p = tree[v].parent;

    if (p == -1)
    {
        root = child;
    }
    else if (tree[p].left == v)
    {
        tree[p].left = child;
    }
    else
    {
        tree[p].right = child;
    }

    if (child != -1)
        tree[child].parent = p;
}

void PreOrderTraversal(Node* tree, int v, ofstream& out)
{
    if (v == -1)
        return;

    out << tree[v].key << '\n';

    PreOrderTraversal(tree, tree[v].left, out);
    PreOrderTraversal(tree, tree[v].right, out);
}

int main()
{
    ifstream in("in.txt");
    ofstream out("out.txt");

    if (!in.is_open() || !out.is_open())
        return 0;

    int capacity = 10;
    int count = 0;
    int root = -1;

    Node* tree = new Node[capacity];

    int key;

    while (in >> key)
    {
        if (count == capacity)
        {
            capacity *= 2;

            Node* temp = new Node[capacity];

            for (int i = 0; i < count; i++)
                temp[i] = tree[i];

            delete[] tree;
            tree = temp;
        }

        Insert(tree, count, key, root);
    }

    for (int i = 0; i < count; i++)
    {
        for (int j = 0; j < 2; j++)
        {
            tree[i].dist[j] = -1;
            tree[i].sum[j] = 0;
            tree[i].leaf[j] = -1;
        }
    }

    int bestLength = INT_MAX;
    long long bestSum = LLONG_MAX;

    int bestRoot = -1;
    int bestLeft = -1;
    int bestRight = -1;

    if (root != -1)
    {
        FindPaths(tree, root,
                  bestLength, bestSum,
                  bestRoot, bestLeft, bestRight);
    }

    if (bestRoot != -1)
    {
        int* path = new int[count];
        int* temp = new int[count];

        int length = 0;
        int tempCount = 0;

        int current = bestLeft;

        while (current != bestRoot)
        {
            path[length++] = current;
            current = tree[current].parent;
        }

        path[length++] = bestRoot;

        current = bestRight;

        while (current != bestRoot)
        {
            temp[tempCount++] = current;
            current = tree[current].parent;
        }

        for (int i = tempCount - 1; i >= 0; i--)
            path[length++] = temp[i];

        int middle = path[length / 2];

        DeleteRight(tree, root, middle);

        delete[] path;
        delete[] temp;
    }

    PreOrderTraversal(tree, root, out);

    delete[] tree;

    in.close();
    out.close();

    return 0;
}
