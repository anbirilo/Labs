#include <windows.h>
#include <iostream>
#include <fstream>
#include <iomanip>
#include <string>
#include <vector>
#include "employee.h"

bool runProcess(const std::string& exePath, const std::string& args)
{
    std::string cmdLine = "\"" + exePath + "\" " + args;

    std::vector<char> cmdBuf(cmdLine.begin(), cmdLine.end());
    cmdBuf.push_back('\0');

    STARTUPINFOA si{};
    si.cb = sizeof(si);
    PROCESS_INFORMATION pi{};

    BOOL ok = CreateProcessA(
        NULL,            // имя исполняемого модуля (NULL - берем из cmdLine)
        cmdBuf.data(),   // командная строка (изменяемая)
        NULL,            // атрибуты безопасности процесса
        NULL,            // атрибуты безопасности потока
        FALSE,           // не наследовать хэндлы
        0,               // без дополнительных флагов создания
        NULL,            // окружение - как у родителя
        NULL,            // рабочий каталог - как у родителя
        &si,
        &pi
    );

    if (!ok)
    {
        std::cerr << "Не удалось запустить \"" << exePath
                  << "\", код ошибки: " << GetLastError() << std::endl;
        return false;
    }

    WaitForSingleObject(pi.hProcess, INFINITE);

    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    return true;
}

void printBinaryFile(const std::string& fileName)
{
    std::ifstream file(fileName, std::ios::binary);
    if (!file)
    {
        std::cerr << "Не удалось открыть файл " << fileName << std::endl;
        return;
    }

    std::cout << "\nСодержимое файла \"" << fileName << "\":" << std::endl;
    std::cout << std::left
               << std::setw(12) << "Номер"
               << std::setw(14) << "Имя"
               << std::setw(10) << "Часы" << std::endl;

    employee e;
    while (file.read(reinterpret_cast<char*>(&e), sizeof(employee)))
    {
        std::cout << std::left
                   << std::setw(12) << e.num
                   << std::setw(14) << e.name
                   << std::setw(10) << e.hours << std::endl;
    }
}

void printTextFile(const std::string& fileName)
{
    std::ifstream file(fileName);
    if (!file)
    {
        std::cerr << "Не удалось открыть файл " << fileName << std::endl;
        return;
    }

    std::cout << "\nСодержимое отчета \"" << fileName << "\":" << std::endl;
    std::string line;
    while (std::getline(file, line))
    {
        std::cout << line << std::endl;
    }
}

int main()
{
    SetConsoleCP(65001);
    SetConsoleOutputCP(65001);

    std::string binFileName;
    int count = 0;

    std::cout << "Введите имя бинарного файла: ";
    std::cin >> binFileName;

    std::cout << "Введите количество записей: ";
    std::cin >> count;

    std::string creatorArgs = binFileName + " " + std::to_string(count);
    if (!runProcess(".\\Creator.exe", creatorArgs))
    {
        return 1;
    }

    printBinaryFile(binFileName);

    std::string reportFileName;
    double rate = 0.0;

    std::cout << "\nВведите имя файла отчета: ";
    std::cin >> reportFileName;

    std::cout << "Введите оплату за час работы: ";
    std::cin >> rate;

    std::string reporterArgs = binFileName + " " + reportFileName + " " + std::to_string(rate);
    if (!runProcess(".\\Reporter.exe", reporterArgs))
    {
        return 1;
    }

    printTextFile(reportFileName);

    std::cout << "\nРабота программы Main завершена." << std::endl;
    return 0;
}