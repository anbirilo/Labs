#include "replacelib.h"

void replace_elements(int* arr, size_t minIdx, size_t maxIdx, int value)
{
    arr[minIdx] = value;
    arr[maxIdx] = value;
}