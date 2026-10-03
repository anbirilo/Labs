#include <windows.h>
#include <iostream>
#include <vector>
#include <memory>
using namespace std;

struct HandleCloser
{
    void operator()(HANDLE h) const { if (h) CloseHandle(h); }
};
using ThreadHandle = unique_ptr<void, HandleCloser>;

struct Data
{
    vector<int> arr;    
    int    min = 0;     
    int    max = 0;     
    size_t minIdx = 0;  
    size_t maxIdx = 0; 
    double avg = 0.0;  
};

DWORD WINAPI min_max(LPVOID param)
{
    Data* d = static_cast<Data*>(param);

    d->min = d->max = d->arr[0];
    d->minIdx = d->maxIdx = 0;

    for (size_t i = 1; i < d->arr.size(); i++)
    {
        if (d->arr[i] < d->min)
        {
            d->min = d->arr[i];
            d->minIdx = i;
        }
        Sleep(7);               

        if (d->arr[i] > d->max)
        {
            d->max = d->arr[i];
            d->maxIdx = i;
        }
        Sleep(7);              
    }

    cout << "min = " << d->min << ", max = " << d->max << endl;
    return 0;
}

DWORD WINAPI average(LPVOID param)
{
    Data* d = static_cast<Data*>(param);

    long long sum = 0;
    for (int x : d->arr)
    {
        sum += x;
        Sleep(12);              
    }
    d->avg = static_cast<double>(sum) / d->arr.size();

    cout << "average = " << d->avg << endl;
    return 0;
}

int main()
{
    Data d;

    int n;
    cout << "Enter the array size: ";
    if (!(cin >> n) || n <= 0)
    {
        cout << "The size must be a positive integer" << endl;
        return 1;
    }

    d.arr.resize(n);
    cout << "Enter the array elements:" << endl;
    for (int& x : d.arr)
    {
        if (!(cin >> x))
        {
            cout << "Input error: integers are required" << endl;
            return 1;
        }
    }

    ThreadHandle hMinMax(CreateThread(nullptr, 0, min_max, &d, 0, nullptr));
    if (!hMinMax)
    {
        cout << "Failed to create the min_max thread" << endl;
        return 1;
    }

    ThreadHandle hAverage(CreateThread(nullptr, 0, average, &d, 0, nullptr));
    if (!hAverage)
    {
        cout << "Failed to create the average thread" << endl;
        WaitForSingleObject(hMinMax.get(), INFINITE);
        return 1;
    }

    WaitForSingleObject(hMinMax.get(), INFINITE);
    WaitForSingleObject(hAverage.get(), INFINITE);

    d.arr[d.minIdx] = static_cast<int>(d.avg);
    d.arr[d.maxIdx] = static_cast<int>(d.avg);

    cout << "Resulting array:" << endl;
    for (int x : d.arr)
        cout << x << " ";
    cout << endl;

    return 0;
}