#include <iostream>
#include <set>
#include <fstream>
#include <string>

int main() {
    std::ifstream fin("input.txt");
    std::string line;
    std::set<long long> array;
    while(getline(fin, line)) {
        if (line.empty()) {
            continue;
        }  
        long long number = std::stoll(line);
        array.insert(number);
    }
    long long sum{};
    for (const auto& element : array) {
        sum += element;
    }

    std::ofstream fout("output.txt");
    fout << sum;
    fout.close();

    return 0;
}
