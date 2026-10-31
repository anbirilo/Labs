#include <fstream>

int main() {
    std::ifstream fin("input.txt");
    std::ofstream fout("output.txt");

    long long n;
    fin >> n;

    int* orders = new int[64];   
    int count = 0;

    for (int bit = 0; n > 0; ++bit) {
        if (n % 2 == 1) {
            orders[count++] = bit;
        }
        n /= 2;
    }

    for (int i = 0; i < count; ++i) {
        fout << orders[i] << "\n";
    }

    delete[] orders;
    return 0;
}