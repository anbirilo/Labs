#pragma once
#include <cstddef>

extern "C" __declspec(dllexport)
void replace_elements(int* arr, size_t minIdx, size_t maxIdx, int value);