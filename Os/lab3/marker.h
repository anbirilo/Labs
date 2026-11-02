#pragma once

#include <windows.h>
#include <vector>

// Всё, что относится к одному потоку marker
struct MarkerParams {
    int    id;          // порядковый номер потока (начиная с 1)
    HANDLE hThread;     // дескриптор потока
    HANDLE hBlocked;    // marker -> main: «не могу продолжать» (ручной сброс)
    HANDLE hContinue;   // main -> marker: «продолжай»          (автосброс)
    HANDLE hTerminate;  // main -> marker: «завершайся»         (автосброс)
    bool   finished;    // поток завершён (меняет и читает только main)
};

extern std::vector<int>          arr;          // общий массив
extern std::vector<MarkerParams> markers;      // markers[i] описывает поток с номером i + 1
extern CRITICAL_SECTION          cs;           // защищает массив и вывод в консоль
extern HANDLE                    hStartEvent;  // main -> все marker: «начинайте работу» (ручной сброс)

// Вывод содержимого массива в консоль
void printArray();

// Функция потока marker. Параметр — порядковый номер потока.
DWORD WINAPI marker(LPVOID param);