#include <iostream>
#include <fstream>
#include <vector>

int main() {
    std::ifstream fin("input.txt");
    std::ofstream fout("output.txt");
    int n;
    fin >> n;
    int* array = new int[n + 1]; 
    for (int i = 1; i <= n; ++i) {        
        fin >> array[i];
    }
    bool isHeap = true;
    for (size_t i = 1; i < n + 1; i++) {

        int left = i * 2;                  
        int right = i * 2 + 1;            
        if ( left <= n && array[i] > array[left] ){
            isHeap = false;
            break;
        }
        if (right <= n && array[i] > array[right]) {
            isHeap = false;
            break;
        }
    }

    if (isHeap) {
        fout << "Yes";
    } else {
        fout << "No";
    }

    delete[] array;                         
}