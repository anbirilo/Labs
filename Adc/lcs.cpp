#include <iostream>
#include <vector>
#include <algorithm>

int main() {
    int n{};
    std::cout<< "Enter n: " << std::endl;
    std::cin >> n;
    std::vector<int> A(n + 1);
    std::vector<int> B(n + 1);
    std::vector<int> indexesA(n + 1);
    std::vector<int> indexesB(n + 1);
    std::vector<std::vector<int>> matrix(n + 1, std::vector<int>(n + 1, 0));
    std::cout << "Enter sequence A: ";
    for (size_t i = 1; i < n + 1; i++)
    {
        std::cin >> A[i];
    }
    std::cout << "Enter sequence B: ";
    for (size_t i = 1; i < n + 1; i++)
    {
        std::cin >> B[i];
    }
    
    for (size_t i = 1; i < n + 1; i++)
    {
        for (size_t j = 1; j < n + 1; j++)
        {
            if (A[i] == B[j]) {
                matrix[i][j] = matrix[i - 1][j - 1] + 1;
                indexesA[i] = i;
                indexesB[j] = j;

            } else {
                matrix[i][j] = std::max(matrix[i - 1][j], matrix[i][j - 1]);
            }
        }
        
    }
    std::cout << matrix[n][n] << std::endl; 
    for (const int& e: indexesA) {
        std::cout << e <<' ';
    }
    std::cout << std::endl;

    for (const int& e: indexesB) {
        std::cout << e <<' ';
    }
    
    return 0;
}
    
