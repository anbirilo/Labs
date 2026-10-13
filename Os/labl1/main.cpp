#include <iostream>
#include <cstdlib>
#include <ctime>
#include <clocale>

using namespace std;

int inputSize(int maxN) {
    int n;
    while (true) {
        cout << "Введите размерность массива n (1.." << maxN << "): ";
        cin >> n;
        if (n >= 1 && n <= maxN) {
            return n;
        }
        cout << "Ошибка ввода! Нужно целое число от 1 до " << maxN << ".\n";
    }
}

void fillRandom(int a[], int n) {
    for (int i = 0; i < n; i++) {
        a[i] = rand() % 201 - 100;
    }
}

void printArray(const int a[], int n) {
    if (n == 0) {
        cout << "(массив пуст)\n";
        return;
    }
    for (int i = 0; i < n; i++) {
        cout << a[i] << " ";
    }
    cout << "\n";
}

void moveElement(int a[], int to, int from) {
    int tmp = a[from];
    for (int k = from; k > to; k--) {
        a[k] = a[k - 1];
    }
    a[to] = tmp;
}

void deleteElement(int a[], int n, int pos) {
    for (int k = pos; k < n - 1; k++) {
        a[k] = a[k + 1];
    }
}

int arrange(int a[], int n) {
    for (int i = 0; i < n; i++) {
        bool needNegative;
        if ((i & 1) == 0) {
            needNegative = true;
        }
        else {
            needNegative = false;
        }

        int j = i;
        while (j < n) {
            if (needNegative && a[j] < 0) {
                break;
            }
            if (!needNegative && a[j] > 0) {
                break;
            }
            j++;
        }

        if (j == n) {
            while (n > i) {
                deleteElement(a, n, i);
                n--;
            }
            return n;
        }
        if (j != i)
        {
            moveElement(a, i, j);
        }
    }
    return n;
}

int main() {
    setlocale(LC_ALL, "Russian");
    srand(time(NULL));
    const int MAX_N = 100;
    int a[MAX_N];
    int n = inputSize(MAX_N);
    fillRandom(a, n);
    cout << "Исходный массив:\n";
    printArray(a, n);
    n = arrange(a, n);
    cout << "Результат:\n";
    printArray(a, n);
    return 0;
}