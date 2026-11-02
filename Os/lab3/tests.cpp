#include <gtest/gtest.h>

#include <windows.h>
#include <iostream>
#include <sstream>
#include <vector>

#include "marker.h"

using namespace std;

// Фикстура: создаёт массив и потоки marker, подаёт им сигналы так же, как main,
// и гарантированно останавливает потоки в конце теста.
class MarkerTest : public ::testing::Test {
protected:
    ostringstream captured;          // сюда уходит вывод потоков marker
    streambuf* oldCout = nullptr;

    void SetUp() override {
        oldCout = cout.rdbuf(captured.rdbuf());
        InitializeCriticalSection(&cs);
        hStartEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
        arr.clear();
        markers.clear();
    }

    void TearDown() override {
        if (!markers.empty()) {
            SetEvent(hStartEvent);               // на случай, если тест не стартовал потоки
            waitAllBlocked();
            for (size_t i = 0; i < markers.size(); ++i)
                if (!markers[i].finished)
                    terminateMarker(markers[i].id);
        }
        CloseHandle(hStartEvent);
        DeleteCriticalSection(&cs);
        cout.rdbuf(oldCout);
    }

    // Массив из n нулей и k потоков marker (сигнал старта пока не подан)
    void init(int n, int k) {
        arr.assign(n, 0);
        markers.assign(k, MarkerParams());
        for (int i = 0; i < k; ++i) {
            markers[i].id         = i + 1;
            markers[i].hBlocked   = CreateEvent(NULL, TRUE,  FALSE, NULL);
            markers[i].hContinue  = CreateEvent(NULL, FALSE, FALSE, NULL);
            markers[i].hTerminate = CreateEvent(NULL, FALSE, FALSE, NULL);
        }
        for (int i = 0; i < k; ++i) {
            DWORD idThread;
            markers[i].hThread = CreateThread(
                NULL, 0, marker,
                reinterpret_cast<LPVOID>(static_cast<INT_PTR>(markers[i].id)),
                0, &idThread);
        }
    }

    void startMarkers() {
        SetEvent(hStartEvent);
    }

    // Ждёт, пока все незавершённые потоки подадут сигнал о невозможности продолжения
    bool waitAllBlocked() {
        vector<HANDLE> h;
        for (size_t i = 0; i < markers.size(); ++i)
            if (!markers[i].finished)
                h.push_back(markers[i].hBlocked);
        if (h.empty())
            return true;
        return WaitForMultipleObjects(static_cast<DWORD>(h.size()), h.data(), TRUE, 5000)
               == WAIT_OBJECT_0;
    }

    // Сигнал на завершение потоку с номером id и ожидание его завершения
    void terminateMarker(int id) {
        MarkerParams& m = markers[id - 1];
        SetEvent(m.hTerminate);
        WaitForSingleObject(m.hThread, 5000);
        CloseHandle(m.hThread);
        CloseHandle(m.hBlocked);
        CloseHandle(m.hContinue);
        CloseHandle(m.hTerminate);
        m.finished = true;
    }

    // Сигнал на продолжение всем незавершённым потокам
    void continueAll() {
        for (size_t i = 0; i < markers.size(); ++i) {
            if (!markers[i].finished) {
                ResetEvent(markers[i].hBlocked);
                SetEvent(markers[i].hContinue);
            }
        }
    }

    vector<int> snapshot() {
        EnterCriticalSection(&cs);
        vector<int> copy = arr;
        LeaveCriticalSection(&cs);
        return copy;
    }
};

// Тест 1: printArray выводит все элементы массива через пробел
TEST_F(MarkerTest, PrintArrayPrintsAllElements) {
    arr = {1, 0, 2};

    printArray();

    EXPECT_EQ(captured.str(), "Массив: 1 0 2 \n");
}

// Тест 2: до сигнала от main потоки marker ничего не делают с массивом
TEST_F(MarkerTest, MarkersDoNothingBeforeStartSignal) {
    init(10, 3);

    Sleep(100);

    EXPECT_EQ(snapshot(), vector<int>(10, 0));
    for (size_t i = 0; i < markers.size(); ++i)
        EXPECT_EQ(WaitForSingleObject(markers[i].hBlocked, 0), static_cast<DWORD>(WAIT_TIMEOUT));
}

// Тест 3: один поток на массиве из одного элемента помечает его своим номером,
// после чего сообщает, что продолжать невозможно (элемент уже занят)
TEST_F(MarkerTest, SingleMarkerMarksCellAndThenBlocks) {
    init(1, 1);
    startMarkers();

    ASSERT_TRUE(waitAllBlocked());

    EXPECT_EQ(snapshot(), vector<int>(1, 1));
    EXPECT_NE(captured.str().find("Marker #1"), string::npos);
}

// Тест 4: после остановки всех потоков в массиве лежат только номера существующих потоков
TEST_F(MarkerTest, ArrayContainsOnlyValidMarkerIds) {
    const int k = 4;
    init(30, k);
    startMarkers();

    ASSERT_TRUE(waitAllBlocked());

    int nonZero = 0;
    vector<int> a = snapshot();
    for (size_t i = 0; i < a.size(); ++i) {
        EXPECT_GE(a[i], 0);
        EXPECT_LE(a[i], k);
        if (a[i] != 0) ++nonZero;
    }
    EXPECT_GT(nonZero, 0);
}

// Тест 5: завершаемый поток зануляет только свои элементы, чужие не трогает
TEST_F(MarkerTest, TerminatedMarkerClearsOnlyItsOwnCells) {
    init(40, 3);
    startMarkers();
    ASSERT_TRUE(waitAllBlocked());

    vector<int> before = snapshot();

    terminateMarker(2);

    vector<int> after = snapshot();
    ASSERT_EQ(before.size(), after.size());
    for (size_t i = 0; i < before.size(); ++i) {
        if (before[i] == 2)
            EXPECT_EQ(after[i], 0) << "ячейка " << i << " должна быть обнулена";
        else
            EXPECT_EQ(after[i], before[i]) << "ячейка " << i << " не должна меняться";
    }
    EXPECT_TRUE(markers[1].finished);
    EXPECT_FALSE(markers[0].finished);
    EXPECT_FALSE(markers[2].finished);
}

// Тест 6: по сигналу «продолжить» поток возобновляет работу и снова блокируется
TEST_F(MarkerTest, MarkerResumesAfterContinueSignal) {
    init(1, 1);
    startMarkers();
    ASSERT_TRUE(waitAllBlocked());

    continueAll();                       // поток обязан проснуться

    ASSERT_TRUE(waitAllBlocked());       // и снова упереться в занятый элемент
    EXPECT_EQ(snapshot(), vector<int>(1, 1));

    // сообщение выведено дважды: до и после сигнала продолжения
    string out = captured.str();
    size_t first = out.find("Marker #1");
    ASSERT_NE(first, string::npos);
    EXPECT_NE(out.find("Marker #1", first + 1), string::npos);
}

// Тест 7: полный сценарий main — завершаем потоки по одному, в конце массив пуст
TEST_F(MarkerTest, TerminatingAllMarkersOneByOneLeavesArrayEmpty) {
    const int n = 20, k = 3;
    init(n, k);
    startMarkers();

    for (int id = 1; id <= k; ++id) {
        ASSERT_TRUE(waitAllBlocked());
        terminateMarker(id);
        EXPECT_TRUE(markers[id - 1].finished);

        // после завершения потока его номера в массиве быть не должно
        vector<int> a = snapshot();
        for (size_t i = 0; i < a.size(); ++i)
            EXPECT_NE(a[i], id);

        continueAll();
    }

    EXPECT_EQ(snapshot(), vector<int>(n, 0));
}