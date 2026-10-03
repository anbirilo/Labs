#include <iostream>
#include <vector>

struct info {
    int b;
    int l;
    int r;
};

int main() {
    int n;
    std::cin >> n;
    std::vector<long long> array(n);
    for (int i = 0; i < n; ++i) {
        std::cin >> array[i];
    }

    int k;
    std::cin >> k;
    std::vector<long long> requests(k);
    for (int i = 0; i < k; ++i) {
        std::cin >> requests[i];
    }

    std::vector<info> logs(k);

    for (int i = 0; i < k; ++i) {
        long long target = requests[i];

        int low = 0, high = n - 1;
        int l = n;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (array[mid] >= target) {
                l = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        low = 0; high = n - 1;
        int r = n;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (array[mid] > target) {
                r = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        logs[i].l = l;
        logs[i].r = r;
        logs[i].b = (l < n && array[l] == target) ? 1 : 0;
    }

    for (const auto& element : logs) {
        std::cout << element.b << ' ' << element.l << ' ' << element.r << '\n';
    }

    return 0;
}