#include <iostream> 
#include <vector> 
#include <algorithm> 
#include <string>

int main() { 
    std::string S; 
    
    int n = S.length(); 
    
    std::string A = " " + S;
    std::string B = S;
    std::reverse(B.begin(), B.end());
    B = " " + B;
    
    std::vector<std::vector<int>> matrix(n + 1, std::vector<int>(n + 1, 0)); 
    
    for (int i = 1; i < n + 1; i++) { 
        for (int j = 1; j < n + 1; j++) { 
            if (A[i] == B[j]) { 
                matrix[i][j] = matrix[i - 1][j - 1] + 1; 
            } else { 
                matrix[i][j] = std::max(matrix[i - 1][j], matrix[i][j - 1]); 
            } 
        } 
    } 
    
    std::cout << matrix[n][n] << "\n"; 
    
    std::string palindrome = "";
    int i = n; 
    int j = n; 
    while (i > 0 && j > 0) { 
        if (A[i] == B[j]) { 
            palindrome.push_back(A[i]); 
            i--; 
            j--; 
        } else if (matrix[i - 1][j] >= matrix[i][j - 1]) { 
            i--; 
        } else { 
            j--; 
        } 
    } 
    

    std::reverse(palindrome.begin(), palindrome.end());
    std::cout << palindrome; 
    
    return 0; 
}
