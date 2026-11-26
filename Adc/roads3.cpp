
#include <iostream>
#include <vector>

using namespace std;

struct Edge
{
    int u, v;
};

vector<int> parent;
vector<int> sz;

int components;

void MakeSet(int n)
{
    parent.resize(n + 1);
    sz.resize(n + 1);

    components = n;

    for (int i = 1; i <= n; i++)
    {
        parent[i] = i;
        sz[i] = 1;
    }
}

int FindSet(int v)
{
    if (parent[v] == v)
        return v;

    parent[v] = FindSet(parent[v]);

    return parent[v];
}

void UnionSet(int a, int b)
{
    a = FindSet(a);
    b = FindSet(b);

    if (a != b)
    {
        if (sz[a] < sz[b])
            swap(a, b);

        parent[b] = a;
        sz[a] += sz[b];

        components--;
    }
}

int main()
{
    int n, m, q;
    cin >> n >> m >> q;

    vector<Edge> edges(m + 1);

    for (int i = 1; i <= m; i++)
    {
        cin >> edges[i].u >> edges[i].v;
    }

    vector<int> destroy(q);
    vector<bool> used(m + 1, false);

    for (int i = 0; i < q; i++)
    {
        cin >> destroy[i];
        used[destroy[i]] = true;
    }

    MakeSet(n);

    for (int i = 1; i <= m; i++)
    {
        if (!used[i])
        {
            UnionSet(edges[i].u, edges[i].v);
        }
    }

    vector<int> answer(q);

    for (int i = q - 1; i >= 0; i--)
    {
        int id = destroy[i];

        UnionSet(edges[id].u, edges[id].v);

        if (components == 1)
            answer[i] = 1;
        else
            answer[i] = 0;
    }

    for (int i = 0; i < q; i++)
    {
        cout << answer[i];
    }

    return 0;
}
