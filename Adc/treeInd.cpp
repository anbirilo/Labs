#include <iostream>
#include <fstream>
#include <climits>

using namespace std;

struct Node
{
    int key;
    char side;
    int parent;

    int dist;
    long long sum;
    int leaf;

    bool active;
};


int FindChild(Node* tree, int count, int parent, char side)
{
    for (int i = 0; i < count; i++)
    {
        if (tree[i].active &&
            tree[i].parent == parent &&
            tree[i].side == side)
        {
            return i;
        }
    }

    return -1;
}

void Insert(Node* tree, int& count, int key, int& root)
{
    if (root == -1)
    {
        tree[count].key = key;
        tree[count].side = '-';
        tree[count].parent = -1;
        tree[count].dist = -1;
        tree[count].sum = 0;
        tree[count].leaf = -1;
        tree[count].active = true;

        root = count;
        count++;

        return;
    }

    int current = root;

    while (true)
    {
        if (key == tree[current].key)
            return;

        char side;

        if (key < tree[current].key)
            side = 'L';
        else
            side = 'R';

        int child = FindChild(tree, count, current, side);

        if (child == -1)
        {
            tree[count].key = key;
            tree[count].side = side;
            tree[count].parent = current;
            tree[count].dist = -1;
            tree[count].sum = 0;
            tree[count].leaf = -1;
            tree[count].active = true;

            count++;

            return;
        }

        current = child;
    }
}

void Update(Node* tree, int v, int child)
{
    int newDist = tree[child].dist + 1;

    long long newSum =
        tree[child].sum + tree[v].key;

    if (tree[v].dist == -1 ||
        newDist < tree[v].dist ||
        (newDist == tree[v].dist &&
         newSum < tree[v].sum))
    {
        tree[v].dist = newDist;
        tree[v].sum = newSum;
        tree[v].leaf = tree[child].leaf;
    }
}


void FindPaths(Node* tree, int count,
               int& bestLength,
               long long& bestSum,
               int& bestRoot,
               int& bestLeft,
               int& bestRight)
{

    for (int v = count - 1; v >= 0; v--)
    {
        if (!tree[v].active)
            continue;

        int l = FindChild(tree, count, v, 'L');
        int r = FindChild(tree, count, v, 'R');

        if (l == -1 && r == -1)
        {
            tree[v].dist = 0;
            tree[v].sum = tree[v].key;
            tree[v].leaf = v;

            continue;
        }

        if (l != -1)
            Update(tree, v, l);

        if (r != -1)
            Update(tree, v, r);

        if (l != -1 && r != -1)
        {
            int length =
                tree[l].dist + tree[r].dist + 2;

            long long sum =
                tree[l].sum + tree[r].sum + tree[v].key;

            if (length < bestLength ||
                (length == bestLength && sum < bestSum) ||
                (length == bestLength && sum == bestSum &&
                 (bestRoot == -1 ||
                  tree[v].key < tree[bestRoot].key)))
            {
                bestLength = length;
                bestSum = sum;

                bestRoot = v;
                bestLeft = tree[l].leaf;
                bestRight = tree[r].leaf;
            }
        }
    }
}

int FindMinimum(Node* tree, int count, int v)
{
    int child = FindChild(tree, count, v, 'L');

    while (child != -1)
    {
        v = child;
        child = FindChild(tree, count, v, 'L');
    }

    return v;
}

void DeleteRight(Node* tree, int count, int& root, int v)
{
    int l = FindChild(tree, count, v, 'L');
    int r = FindChild(tree, count, v, 'R');

    if (l != -1 && r != -1)
    {
        int successor = FindMinimum(tree, count, r);

        tree[v].key = tree[successor].key;

        v = successor;
    }
    int left = FindChild(tree, count, v, 'L');
    int right = FindChild(tree, count, v, 'R');

    int child;

    if (left != -1)
        child = left;
    else
        child = right;

    int p = tree[v].parent;
    char side = tree[v].side;

    if (p == -1)
    {
        root = child;

        if (child != -1)
        {
            tree[child].parent = -1;
            tree[child].side = '-';
        }
    }
    else if (child != -1)
    {
        tree[child].parent = p;
        tree[child].side = side;
    }

    tree[v].active = false;
}

void PreOrderTraversal(Node* tree, int count,
                       int root, ofstream& out)
{
    if (root == -1)
        return;

    int* stack = new int[count];
    int top = 0;

    stack[top++] = root;

    while (top > 0)
    {
        int v = stack[--top];

        out << tree[v].key << '\n';

        int r = FindChild(tree, count, v, 'R');
        int l = FindChild(tree, count, v, 'L');

        if (r != -1)
            stack[top++] = r;

        if (l != -1)
            stack[top++] = l;
    }

    delete[] stack;
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

    int bestLength = INT_MAX;
    long long bestSum = LLONG_MAX;

    int bestRoot = -1;
    int bestLeft = -1;
    int bestRight = -1;

    FindPaths(tree, count,
              bestLength, bestSum,
              bestRoot, bestLeft, bestRight);

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


        if (bestLength % 2 == 0)
        {
            int middle = path[length / 2];

            DeleteRight(tree, count, root, middle);
        }

        delete[] path;
        delete[] temp;
    }

    PreOrderTraversal(tree, count, root, out);

    delete[] tree;

    in.close();
    out.close();

    return 0;
}