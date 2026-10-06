#include <iostream> 
#include <vector> 
#include <algorithm> 

int main() { 
    int n{}; 
    std::cin >> n; 
    
    std::vector<int> A(n + 1); 
    std::vector<int> B(n + 1); 
    std::vector<int> indexesA;
    std::vector<int> indexesB; 
    std::vector<std::vector<int>> matrix(n + 1, std::vector<int>(n + 1, 0)); 
    
    for (size_t i = 1; i < n + 1; i++) { 
        std::cin >> A[i]; 
    } 
    for (size_t i = 1; i < n + 1; i++) { 
        std::cin >> B[i]; 
    } 
    
    for (size_t i = 1; i < n + 1; i++) { 
        for (size_t j = 1; j < n + 1; j++) { 
            if (A[i] == B[j]) { 
                matrix[i][j] = matrix[i - 1][j - 1] + 1; 
            } else { 
                matrix[i][j] = std::max(matrix[i - 1][j], matrix[i][j - 1]); 
            } 
        } 
    } 
    
    std::cout << matrix[n][n] << std::endl; 
    
    int i = n; 
    int j = n; 
    while (i > 0 && j > 0) { 
        if (A[i] == B[j]) { 
            indexesA.push_back(i - 1); 
            indexesB.push_back(j - 1); 
            i--; 
            j--; 
        } else if (matrix[i - 1][j] >= matrix[i][j - 1]) { 
            i--; 
        } else { 
            j--; 
        } 
    } 
    
    for (size_t k = indexesA.size(); k > 0; k--) { 
        std::cout << indexesA[k - 1] << ' '; 
    } 
    std::cout << std::endl; 
    
    for (size_t k = indexesB.size(); k > 0; k--) { 
        std::cout << indexesB[k - 1] << ' '; 
    } 
    
    return 0; 
}
