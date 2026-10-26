#include <iostream>
#include <vector>
#include <memory>

#include "marker.h"

using namespace std;

int main() {
    // 1, 2. Массив, инициализированный нулями
    int n;
    cout << "Размерность массива: ";
    cin >> n;
    arr.assign(n, 0);

    // 3. Количество потоков marker
    int k;
    cout << "Количество потоков marker: ";
    cin >> k;

    // 4. Запуск потоков (параметр — порядковый номер, начиная с 1)
    vector<unique_ptr<MarkerInfo>> markers;
    for (int i = 1; i <= k; ++i) {
        markers.push_back(unique_ptr<MarkerInfo>(new MarkerInfo()));
        markers.back()->id = i;
    }
    for (int i = 0; i < k; ++i)
        markers[i]->th = thread(marker, markers[i].get());

    // 5. Сигнал на начало работы
    {
        lock_guard<mutex> lk(mtx);
        started = true;
    }
    cv.notify_all();

    // 6. Основной цикл
    int active = k;
    while (active > 0) {
        // 6.1 Ждём, пока все оставшиеся потоки не заблокируются
        {
            unique_lock<mutex> lk(mtx);
            cv.wait(lk, [&] {
                for (int i = 0; i < k; ++i)
                    if (!markers[i]->finished && !markers[i]->blocked)
                        return false;
                return true;
            });
            // 6.2
            cout << "Массив: ";
            printArray();
        }

        // 6.3 Номер потока для завершения
        int num;
        while (true) {
            cout << "Номер потока marker для завершения: ";
            cin >> num;
            if (num >= 1 && num <= k && !markers[num - 1]->finished)
                break;
            cout << "Нет такого активного потока, повторите ввод." << endl;
        }
        MarkerInfo* target = markers[num - 1].get();

        // 6.4 Сигнал на завершение
        {
            lock_guard<mutex> lk(mtx);
            target->cmd = Cmd::Terminate;
        }
        cv.notify_all();

        // 6.5 Ждём завершения потока
        target->th.join();
        --active;

        {
            lock_guard<mutex> lk(mtx);
            // 6.6
            cout << "Массив: ";
            printArray();

            // 6.7 Сигнал на продолжение остальным
            for (int i = 0; i < k; ++i) {
                if (!markers[i]->finished) {
                    markers[i]->blocked = false;
                    markers[i]->cmd = Cmd::Continue;
                }
            }
        }
        cv.notify_all();
    }

    // 7. Все потоки завершены
    cout << "Все потоки marker завершили работу." << endl;
    return 0;
}