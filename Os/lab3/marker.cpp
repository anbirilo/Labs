#include "marker.h"

#include <iostream>
#include <chrono>
#include <cstdlib>

using namespace std;

vector<int> arr;            // общий массив
mutex mtx;                  // защищает массив, консоль и состояния потоков
condition_variable cv;
bool started = false;       // сигнал на начало работы от main

static void sleepMs(int ms) {
    this_thread::sleep_for(chrono::milliseconds(ms));
}

void printArray() {
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