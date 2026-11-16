// Лабораторная работа №4. Синхронизация процессов.
// Процесс Receiver (Windows, WinAPI).

#include <windows.h>
#include <cstdlib>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>

using namespace std;

const int MSG_LEN = 20;  // максимальная длина сообщения (с завершающим нулём)

// Название переменной среды для доп. задания - ВАША ФАМИЛИЯ (латиницей)
const char* ENV_VAR_NAME = "Ivanov";

// Заголовок файла: параметры кольцевой очереди FIFO
struct Header {
    int capacity;  // количество записей в файле
    int head;      // индекс записи, из которой читает Receiver
    int tail;      // индекс записи, в которую пишет следующий Sender
};

// Имена объектов синхронизации (общие для Receiver и Sender)
const char* MUTEX_NAME = "Lab4_FileMutex";
const char* EMPTY_SEM_NAME = "Lab4_EmptySem";  // число свободных записей
const char* FULL_SEM_NAME = "Lab4_FullSem";    // число занятых записей
const char* READY_SEM_NAME = "Lab4_ReadySem";  // сигналы готовности от Sender

int main() {
    // 1. Получение имени бинарного файла (с консоли или из переменной среды)
    //    и ввод количества записей
    string fileName;
    int capacity;

    cout << "1 - enter file name from console\n"
         << "2 - get file name from environment variable\n> ";
    int choice;
    cin >> choice;

    if (choice == 1) {
        cout << "Enter binary file name: ";
        cin >> fileName;
    }
    else if (choice == 2) {
        char* buf = nullptr;
        size_t len = 0;
        // Название переменной среды - ваша фамилия (замените "Ivanov")
        if (_dupenv_s(&buf, &len, ENV_VAR_NAME) != 0 || buf == nullptr) {
            cout << "Environment variable '" << ENV_VAR_NAME << "' not found.\n";
            return 1;
        }
        fileName = buf;
        free(buf);  // _dupenv_s выделяет память динамически - освобождаем
        cout << "File name from environment: " << fileName << "\n";
    }
    else {
        cout << "Unknown choice.\n";
        return 1;
    }

    cout << "Enter number of records: ";
    cin >> capacity;
    if (capacity <= 0) {
        cout << "Number of records must be positive.\n";
        return 1;
    }

    // 2. Создание бинарного файла для сообщений
    {
        ofstream fout(fileName, ios::binary | ios::trunc);
        if (!fout) {
            cout << "Cannot create file.\n";
            return 1;
        }
        Header h = { capacity, 0, 0 };
        fout.write(reinterpret_cast<char*>(&h), sizeof(h));
        char empty[MSG_LEN] = { 0 };
        for (int i = 0; i < capacity; ++i)
            fout.write(empty, MSG_LEN);
    }

    // 3. Ввод количества процессов Sender
    int senderCount;
    cout << "Enter number of Sender processes: ";
    cin >> senderCount;
    if (senderCount <= 0) {
        cout << "Number of Sender processes must be positive.\n";
        return 1;
    }

    // Объекты синхронизации создаём ДО запуска Sender
    HANDLE hMutex = CreateMutexA(NULL, FALSE, MUTEX_NAME);
    HANDLE hEmpty = CreateSemaphoreA(NULL, capacity, capacity, EMPTY_SEM_NAME);
    HANDLE hFull = CreateSemaphoreA(NULL, 0, capacity, FULL_SEM_NAME);
    HANDLE hReady = CreateSemaphoreA(NULL, 0, senderCount, READY_SEM_NAME);
    if (!hMutex || !hEmpty || !hFull || !hReady) {
        cout << "Cannot create synchronization objects.\n";
        return 1;
    }

    // 4. Запуск процессов Sender (имя файла передаём через командную строку)
    vector<HANDLE> processes;
    for (int i = 0; i < senderCount; ++i) {
        string cmd = "Sender.exe " + fileName;
        STARTUPINFOA si;
        PROCESS_INFORMATION pi;
        ZeroMemory(&si, sizeof(si));
        si.cb = sizeof(si);
        ZeroMemory(&pi, sizeof(pi));
        if (!CreateProcessA(NULL, &cmd[0], NULL, NULL, FALSE,
                            CREATE_NEW_CONSOLE, NULL, NULL, &si, &pi)) {
            cout << "Cannot start Sender #" << i + 1 << "\n";
            return 1;
        }
        CloseHandle(pi.hThread);
        processes.push_back(pi.hProcess);
    }

    // 5. Ждём сигнал готовности от всех процессов Sender
    for (int i = 0; i < senderCount; ++i)
        WaitForSingleObject(hReady, INFINITE);
    cout << "All Sender processes are ready.\n";

    // 6. Цикл по командам с консоли: чтение сообщения или завершение работы
    while (true) {
        cout << "\n1 - read message\n0 - exit\n> ";
        int cmd;
        if (!(cin >> cmd) || cmd == 0)
            break;
        if (cmd != 1) {
            cout << "Unknown command.\n";
            continue;
        }

        // Если файл пуст — ждём появления сообщения
        WaitForSingleObject(hFull, INFINITE);
        WaitForSingleObject(hMutex, INFINITE);

        fstream f(fileName, ios::binary | ios::in | ios::out);
        Header h;
        f.read(reinterpret_cast<char*>(&h), sizeof(h));

        char msg[MSG_LEN + 1] = { 0 };
        f.seekg(sizeof(Header) + static_cast<streamoff>(h.head) * MSG_LEN);
        f.read(msg, MSG_LEN);

        // Сдвигаем голову очереди по кольцу
        h.head = (h.head + 1) % h.capacity;
        f.seekp(0);
        f.write(reinterpret_cast<char*>(&h), sizeof(h));
        f.close();

        ReleaseMutex(hMutex);
        ReleaseSemaphore(hEmpty, 1, NULL);  // освободилась одна запись

        cout << "Received: " << msg << "\n";
    }

    // Завершение работы
    for (HANDLE p : processes)
        CloseHandle(p);
    CloseHandle(hMutex);
    CloseHandle(hEmpty);
    CloseHandle(hFull);
    CloseHandle(hReady);
    return 0;
}