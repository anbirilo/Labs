#include <iostream>
#include <fstream>
#include <cstdlib>
#include <cstring>
#include <windows.h>
#include "employee.h"

int main(int argc, char* argv[])
{
    SetConsoleCP(65001);
    SetConsoleOutputCP(65001);

    if (argc < 3)
    {
        std::cerr << "Использование: Creator.exe <имя_файла> <кол-во_записей>" << std::endl;
        return 1;
    }

    const char* fileName = argv[1];
    int count = std::atoi(argv[2]);

    if (count <= 0)
    {
        std::cerr << "Некорректное количество записей: " << argv[2] << std::endl;
        return 1;
    }

    std::ofstream file(fileName, std::ios::binary);
    if (!file)
    {
        std::cerr << "Не удалось создать файл " << fileName << std::endl;
        return 1;
    }

    std::cout << "Creator: создание файла \"" << fileName << "\", записей: " << count << std::endl;

    for (int i = 0; i < count; ++i)
    {
        employee e{};
        char nameBuf[128];

        std::cout << "\n--- Запись " << i + 1 << " из " << count << " ---" << std::endl;

        std::cout << "Идентификационный номер: ";
        std::cin >> e.num;

        std::cout << "Имя сотрудника (до 9 символов): ";
        std::cin >> nameBuf;

        std::strncpy(e.name, nameBuf, sizeof(e.name) - 1);
        e.name[sizeof(e.name) - 1] = '\0';

        std::cout << "Отработанные часы: ";
        std::cin >> e.hours;

        file.write(reinterpret_cast<char*>(&e), sizeof(employee));
        if (!file)
        {
            std::cerr << "Ошибка записи в файл " << fileName << std::endl;
            return 1;
        }
    }

    file.close();
    std::cout << "\nCreator: файл \"" << fileName << "\" успешно создан." << std::endl;
    return 0;
}