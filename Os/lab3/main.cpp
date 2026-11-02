#include <windows.h>
#include <iostream>
#include <vector>

#include "marker.h"

using namespace std;

int main() {
    SetConsoleOutputCP(CP_UTF8);   // чтобы русский текст в консоли читался

    // 1, 2. Память под массив, инициализация нулями
    int n;
    cout << "Размерность массива: ";
    cin >> n;
    if (n <= 0) {
        cout << "Размерность должна быть положительной." << endl;
        return 1;
    }
    arr.assign(n, 0);

    // 3. Количество потоков marker
    // (WaitForMultipleObjects принимает не более MAXIMUM_WAIT_OBJECTS = 64 дескрипторов)
    int k;
    cout << "Количество потоков marker: ";
    cin >> k;
    if (k < 1 || k > MAXIMUM_WAIT_OBJECTS) {
        cout << "Количество потоков должно быть от 1 до " << MAXIMUM_WAIT_OBJECTS << "." << endl;
        return 1;
    }

    InitializeCriticalSection(&cs);
    hStartEvent = CreateEvent(NULL, TRUE, FALSE, NULL);      // ручной сброс
    if (hStartEvent == NULL)
        return GetLastError();

    // 4. Запуск потоков; каждому в качестве параметра передаётся порядковый номер
    markers.assign(k, MarkerParams());
    for (int i = 0; i < k; ++i) {
        markers[i].id         = i + 1;
        markers[i].finished   = false;
        markers[i].hBlocked   = CreateEvent(NULL, TRUE,  FALSE, NULL);
        markers[i].hContinue  = CreateEvent(NULL, FALSE, FALSE, NULL);
        markers[i].hTerminate = CreateEvent(NULL, FALSE, FALSE, NULL);
        if (!markers[i].hBlocked || !markers[i].hContinue || !markers[i].hTerminate)
            return GetLastError();
    }
    for (int i = 0; i < k; ++i) {
        DWORD idThread;
        markers[i].hThread = CreateThread(
            NULL, 0, marker,
            reinterpret_cast<LPVOID>(static_cast<INT_PTR>(markers[i].id)),
            0, &idThread);
        if (markers[i].hThread == NULL)
            return GetLastError();
    }

    // 5. Сигнал на начало работы всех потоков marker
    SetEvent(hStartEvent);

    // 6. Основной цикл
    int active = k;
    while (active > 0) {
        // 6.1 Ждём, пока все работающие потоки не подадут сигнал о невозможности продолжения
        vector<HANDLE> blocked;
        for (int i = 0; i < k; ++i)
            if (!markers[i].finished)
                blocked.push_back(markers[i].hBlocked);
        WaitForMultipleObjects(static_cast<DWORD>(blocked.size()), blocked.data(), TRUE, INFINITE);

        // 6.2 Содержимое массива
        printArray();

        // 6.3 Номер потока marker, которому будет подан сигнал на завершение
        int num;
        while (true) {
            cout << "Номер потока marker для завершения: ";
            cin >> num;
            if (num >= 1 && num <= k && !markers[num - 1].finished)
                break;
            cout << "Нет такого активного потока, повторите ввод." << endl;
        }
        MarkerParams& target = markers[num - 1];

        // 6.4 Сигнал на завершение
        SetEvent(target.hTerminate);

        // 6.5 Ждём завершения этого потока
        WaitForSingleObject(target.hThread, INFINITE);
        CloseHandle(target.hThread);
        CloseHandle(target.hBlocked);
        CloseHandle(target.hContinue);
        CloseHandle(target.hTerminate);
        target.finished = true;
        --active;

        // 6.6 Содержимое массива
        printArray();

        // 6.7 Сигнал на продолжение работы остальным потокам.
        // Сначала сбрасываем «заблокирован», потом подаём сигнал — иначе на следующем витке
        // main увидел бы старый сигнал и не стал бы ждать.
        for (int i = 0; i < k; ++i) {
            if (!markers[i].finished) {
                ResetEvent(markers[i].hBlocked);
                SetEvent(markers[i].hContinue);
            }
        }
    }

    // 7. Все потоки marker завершили работу
    cout << "Все потоки marker завершили работу." << endl;

    DeleteCriticalSection(&cs);
    CloseHandle(hStartEvent);
    return 0;
}