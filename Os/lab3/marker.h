#pragma once

#include <vector>
#include <thread>
#include <mutex>
#include <condition_variable>

// Сигнал от main к потоку marker
enum class Cmd { Wait, Continue, Terminate };

struct MarkerInfo {
    int id;                 // порядковый номер потока
    Cmd cmd = Cmd::Wait;    // ответный сигнал от main
    bool blocked = false;   // поток подал сигнал о невозможности продолжения
    bool finished = false;  // поток завершил работу
    std::thread th;
};

extern std::vector<int> arr;            // общий массив
extern std::mutex mtx;                  // защищает массив, консоль и состояния потоков
extern std::condition_variable cv;
extern bool started;                    // сигнал на начало работы от main

void printArray();
void marker(MarkerInfo* self);