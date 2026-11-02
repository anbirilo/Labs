#include "marker.h"

#include <iostream>
#include <cstdlib>

using namespace std;

vector<int>          arr;
vector<MarkerParams> markers;
CRITICAL_SECTION     cs;
HANDLE               hStartEvent;

void printArray() {
    EnterCriticalSection(&cs);
    cout << "Массив: ";
    for (size_t i = 0; i < arr.size(); ++i)
        cout << arr[i] << ' ';
    cout << endl;
    LeaveCriticalSection(&cs);
}

DWORD WINAPI marker(LPVOID param) {
    // Параметр потока — его порядковый номер
    int id = static_cast<int>(reinterpret_cast<INT_PTR>(param));
    MarkerParams& self = markers[id - 1];

    // 1. Начать работу по сигналу от main
    WaitForSingleObject(hStartEvent, INFINITE);

    // 2. Инициализировать генерацию случайных чисел своим порядковым номером
    srand(id);

    int marked = 0;  // количество помеченных элементов

    // 3. Работать циклически
    while (true) {
        // 3.1, 3.2 Случайное число по модулю размерности массива
        int idx = rand() % static_cast<int>(arr.size());

        EnterCriticalSection(&cs);
        if (arr[idx] == 0) {                        // 3.3
            Sleep(5);                               // 3.3.1
            arr[idx] = id;                          // 3.3.2
            ++marked;
            LeaveCriticalSection(&cs);
            Sleep(5);                               // 3.3.3
            continue;                               // 3.3.4
        }

        // 3.4 Элемент уже помечен
        cout << "Marker #" << id
             << ": помечено элементов = " << marked
             << ", невозможно пометить индекс = " << idx << endl;   // 3.4.1
        LeaveCriticalSection(&cs);

        SetEvent(self.hBlocked);                    // 3.4.2

        // 3.4.3 Ждём ответный сигнал: индекс 0 — завершение, индекс 1 — продолжение
        HANDLE commands[2] = { self.hTerminate, self.hContinue };
        DWORD r = WaitForMultipleObjects(2, commands, FALSE, INFINITE);

        if (r == WAIT_OBJECT_0) {                   // 4. Сигнал на завершение
            EnterCriticalSection(&cs);
            for (size_t i = 0; i < arr.size(); ++i) // 4.1
                if (arr[i] == id)
                    arr[i] = 0;
            LeaveCriticalSection(&cs);
            return 0;                               // 4.2
        }
        // 5. Сигнал на продолжение — новый виток цикла
    }
}