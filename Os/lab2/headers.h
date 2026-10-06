#pragma once
#include <windows.h>
#include <vector>
#include <memory>
using namespace std;

struct HandleCloser
{
    void operator()(HANDLE h) const {
        if (h) {
            CloseHandle(h);
        }
    }
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

DWORD WINAPI min_max(LPVOID param);
DWORD WINAPI average(LPVOID param);
typedef void (*ReplaceFunc)(int*, size_t, size_t, int);