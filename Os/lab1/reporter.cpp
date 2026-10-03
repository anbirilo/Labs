#include <iostream>
#include <fstream>
#include <vector>
#include <algorithm>
#include <iomanip>
#include <cstdlib>
#include <windows.h>
#include "employee.h"

int main(int argc, char* argv[])
{
    SetConsoleCP(65001);
    SetConsoleOutputCP(65001);

    if (argc < 4)
    {
        std::cerr << "Использование: Reporter.exe <бинарный_файл> <файл_отчета> <оплата_за_час>" << std::endl;
        return 1;
    }

    const char* binFileName    = argv[1];
    const char* reportFileName = argv[2];
    double rate = std::atof(argv[3]);

    std::ifstream binFile(binFileName, std::ios::binary);
    if (!binFile)
    {
        std::cerr << "Не удалось открыть файл " << binFileName << std::endl;
        return 1;
    }

    std::vector<employee> employees;
    employee e;
    while (binFile.read(reinterpret_cast<char*>(&e), sizeof(employee)))
    {
        employees.push_back(e);
    }
    binFile.close();

    std::sort(employees.begin(), employees.end(),
        [](const employee& a, const employee& b)
        {
            return a.num < b.num;
        });

    std::ofstream reportFile(reportFileName);
    if (!reportFile)
    {
        std::cerr << "Не удалось создать файл отчета " << reportFileName << std::endl;
        return 1;
    }

    reportFile << "Отчет по файлу " << binFileName << std::endl << std::endl;

    reportFile << std::left
               << std::setw(12) << "Номер"
               << std::setw(14) << "Имя"
               << std::setw(10) << "Часы"
               << std::setw(12) << "Зарплата" << std::endl;

    for (const auto& emp : employees)
    {
        double salary = emp.hours * rate;
        reportFile << std::left
                   << std::setw(12) << emp.num
                   << std::setw(14) << emp.name
                   << std::setw(10) << std::fixed << std::setprecision(2) << emp.hours
                   << std::setw(12) << std::fixed << std::setprecision(2) << salary
                   << std::endl;
    }

    reportFile.close();

    std::cout << "Reporter: отчет \"" << reportFileName << "\" успешно создан "
              << "(записей: " << employees.size() << ", ставка: " << rate << ")." << std::endl;
    return 0;
}