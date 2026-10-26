#include <iostream>
#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <cstdlib>
#include <memory>

using namespace std;

enum class Cmd { Wait, Continue, Terminate };

struct MarkerInfo {
    int id;                 // порядковый номер потока
    Cmd cmd = Cmd::Wait;    // ответный сигнал от main
    bool blocked = false;   // поток подал сигнал о невозможности продолжения
    bool finished = false;  // поток завершил работу
    thread th;
};

vector<int> arr;            // общий массив
mutex mtx;                  // защищает массив, консоль и состояния потоков
condition_variable cv;
bool started = false;       // сигнал на начало работы от main

static void sleepMs(int ms) {
    this_thread::sleep_for(chrono::milliseconds(ms));
}

static void printArray() {
    for (size_t i = 0; i < arr.size(); ++i)
        cout << arr[i] << ' ';
    cout << endl;
}

void marker(MarkerInfo* self) {
    // 1. Ждём сигнал на начало работы от main
    {
        unique_lock<mutex> lk(mtx);
        cv.wait(lk, [] { return started; });
    }

    // 2. Инициализация генератора случайных чисел
    srand(self->id);

    int marked = 0;  // количество помеченных элементов

    // 3. Основной цикл
    while (true) {
        int idx = rand() % static_cast<int>(arr.size());   // 3.1, 3.2

        unique_lock<mutex> lk(mtx);
        if (arr[idx] == 0) {                               // 3.3
            sleepMs(5);                                    // 3.3.1
            arr[idx] = self->id;                           // 3.3.2
            ++marked;
            lk.unlock();
            sleepMs(5);                                    // 3.3.3
            continue;                                      // 3.3.4
        }

        // 3.4 Элемент уже занят
        cout << "Marker #" << self->id
             << ": помечено элементов = " << marked
             << ", невозможно пометить индекс = " << idx << endl;   // 3.4.1

        self->cmd = Cmd::Wait;
        self->blocked = true;                              // 3.4.2
        cv.notify_all();

        cv.wait(lk, [&] { return self->cmd != Cmd::Wait; });   // 3.4.3

        if (self->cmd == Cmd::Terminate) {                 // 4
            for (size_t i = 0; i < arr.size(); ++i)        // 4.1
                if (arr[i] == self->id)
                    arr[i] = 0;
            self->finished = true;                         // 4.2
            cv.notify_all();
            return;
        }
        // 5. Continue -> продолжаем цикл
    }
}

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