#include <iostream>
#include <fstream>
#include <vector>

int main() {
    std::ifstream fin("input.txt");
    std::ofstream fout("output.txt");
    int n;
    fin >> n;
    int* array = new int[n + 1]; 
    for (int i = 1; i < n; ++i) {
        fin >> array[i];
    }
    bool isHeap = true;
   for (size_t i = 1; i < n + 1; i++) {

        int left = array[i * 2];
        int right = array[i*2 + 1];
        if (array[i] > array[left] || array[i] > array[right]) {
            isHeap = false;
            break;
        }
        
   }
   if (isHeap) {
        fout << "Yes";
   } else {
        fout <<"No";
   }
  
    
}