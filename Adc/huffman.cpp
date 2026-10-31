#include <iostream>
#include <fstream>
#include <vector>

int main() {
    std::ifstream fin("huffman.in");
    
    int n;
    fin >> n;
    int* array = new int[n + 1]; 
    for (int i = 1; i < n; ++i) {
        fin >> array[i];
    }
    
    
}