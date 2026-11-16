// Лабораторная работа №4. Синхронизация процессов.
// Процесс Sender (Windows, WinAPI).

#include <windows.h>
#include <iostream>
#include <fstream>
#include <string>
#include <cstring>

using namespace std;

const int MSG_LEN = 20;  // максимальная длина сообщения (с завершающим нулём)

// Заголовок файла: параметры кольцевой очереди FIFO (как в Receiver)
struct Header {
    int capacity;
    int head;
    int tail;
};

const char* MUTEX_NAME = "Lab4_FileMutex";
const char* EMPTY_SEM_NAME = "Lab4_EmptySem";
const char* FULL_SEM_NAME = "Lab4_FullSem";
const char* READY_SEM_NAME = "Lab4_ReadySem";

int main(int argc, char* argv[]) {
    // 1. Имя файла получаем из командной строки
    if (argc < 2) {
        cout << "Usage: Sender.exe <binary file name>\n";
        return 1;
    }
    string fileName = argv[1];

    // Открываем объекты синхронизации, созданные процессом Receiver
    HANDLE hMutex = OpenMutexA(SYNCHRONIZE | MUTEX_MODIFY_STATE, FALSE, MUTEX_NAME);
    HANDLE hEmpty = OpenSemaphoreA(SYNCHRONIZE | SEMAPHORE_MODIFY_STATE, FALSE, EMPTY_SEM_NAME);
    HANDLE hFull = OpenSemaphoreA(SYNCHRONIZE | SEMAPHORE_MODIFY_STATE, FALSE, FULL_SEM_NAME);
    HANDLE hReady = OpenSemaphoreA(SYNCHRONIZE | SEMAPHORE_MODIFY_STATE, FALSE, READY_SEM_NAME);
    if (!hMutex || !hEmpty || !hFull || !hReady) {
        cout << "Cannot open synchronization objects.\n";
        system("pause");
        return 1;
    }

    // Проверяем, что файл открывается
    {
        ifstream test(fileName, ios::binary);
        if (!test) {
            cout << "Cannot open file " << fileName << "\n";
            system("pause");
            return 1;
        }
    }

    // 2. Отправляем Receiver сигнал о готовности к работе
    ReleaseSemaphore(hReady, 1, NULL);

    // 3. Цикл по командам с консоли: отправить сообщение или завершить работу
    while (true) {
        cout << "\n1 - send message\n0 - exit\n> ";
        int cmd;
        if (!(cin >> cmd) || cmd == 0)
            break;
        if (cmd != 1) {
            cout << "Unknown command.\n";
            continue;
        }

        // Текст сообщения вводится с консоли, длина меньше 20 символов
        string text;
        cout << "Enter message (less than " << MSG_LEN << " characters): ";
        cin >> text;
        if (text.size() >= static_cast<size_t>(MSG_LEN)) {
            cout << "Message is too long.\n";
            continue;
        }

        // Если файл заполнен — ждём свободную запись
        WaitForSingleObject(hEmpty, INFINITE);
        WaitForSingleObject(hMutex, INFINITE);

        fstream f(fileName, ios::binary | ios::in | ios::out);
        Header h;
        f.read(reinterpret_cast<char*>(&h), sizeof(h));

        char msg[MSG_LEN] = { 0 };
        strncpy_s(msg, text.c_str(), MSG_LEN - 1);
        f.seekp(sizeof(Header) + static_cast<streamoff>(h.tail) * MSG_LEN);
        f.write(msg, MSG_LEN);

        // Сдвигаем хвост очереди по кольцу
        h.tail = (h.tail + 1) % h.capacity;
        f.seekp(0);
        f.write(reinterpret_cast<char*>(&h), sizeof(h));
        f.close();

        ReleaseMutex(hMutex);
        ReleaseSemaphore(hFull, 1, NULL);  // появилось одно сообщение

        cout << "Message sent.\n";
    }

    CloseHandle(hMutex);
    CloseHandle(hEmpty);
    CloseHandle(hFull);
    CloseHandle(hReady);
    return 0;
}