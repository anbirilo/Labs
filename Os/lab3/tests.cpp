#include <gtest/gtest.h>

#include <chrono>
#include <iostream>
#include <memory>
#include <sstream>
#include <vector>

#include "marker.h"

using namespace std;

// Фикстура: создаёт массив и потоки marker, умеет подавать им сигналы
// так же, как это делает main, и гарантированно останавливает потоки в конце теста.
class MarkerTest : public ::testing::Test {
protected:
    vector<unique_ptr<MarkerInfo>> markers;
    ostringstream captured;          // сюда уходит вывод потоков marker
    streambuf* oldCout = nullptr;

    void SetUp() override {
        oldCout = cout.rdbuf(captured.rdbuf());
        started = false;
    }

    void TearDown() override {
        // Доводим всех до состояния «заблокирован» и завершаем
        if (!markers.empty()) {
            startMarkers();
            waitAllBlocked();
            for (auto& m : markers)
                if (!m->finished)
                    terminate(m->id);
        }
        cout.rdbuf(oldCout);
    }

    // Создаёт массив из n нулей и k потоков marker (пока без сигнала старта)
    void init(int n, int k) {
        arr.assign(n, 0);
        for (int i = 1; i <= k; ++i) {
            markers.push_back(unique_ptr<MarkerInfo>(new MarkerInfo()));
            markers.back()->id = i;
        }
        for (auto& m : markers)
            m->th = thread(marker, m.get());
    }

    void startMarkers() {
        {
            lock_guard<mutex> lk(mtx);
            started = true;
        }
        cv.notify_all();
    }

    // Ждёт, пока все незавершённые потоки подадут сигнал о невозможности продолжения
    bool waitAllBlocked() {
        unique_lock<mutex> lk(mtx);
        return cv.wait_for(lk, chrono::seconds(5), [&] {
            for (auto& m : markers)
                if (!m->finished && !m->blocked)
                    return false;
            return true;
        });
    }

    // Сигнал на завершение потоку с номером id и ожидание его завершения
    void terminate(int id) {
        MarkerInfo* m = markers[id - 1].get();
        {
            lock_guard<mutex> lk(mtx);
            m->cmd = Cmd::Terminate;
        }
        cv.notify_all();
        m->th.join();
    }

    // Сигнал на продолжение всем незавершённым потокам
    void continueAll() {
        {
            lock_guard<mutex> lk(mtx);
            for (auto& m : markers) {
                if (!m->finished) {
                    m->blocked = false;
                    m->cmd = Cmd::Continue;
                }
            }
        }
        cv.notify_all();
    }

    vector<int> snapshot() {
        lock_guard<mutex> lk(mtx);
        return arr;
    }
};

// Тест 1: printArray выводит все элементы массива через пробел
TEST_F(MarkerTest, PrintArrayPrintsAllElements) {
    arr = {1, 0, 2};
    captured.str("");

    printArray();

    EXPECT_EQ(captured.str(), "1 0 2 \n");
}

// Тест 2: до сигнала от main потоки marker ничего не делают с массивом
TEST_F(MarkerTest, MarkersDoNothingBeforeStartSignal) {
    init(10, 3);

    this_thread::sleep_for(chrono::milliseconds(100));

    EXPECT_EQ(snapshot(), vector<int>(10, 0));
    for (auto& m : markers)
        EXPECT_FALSE(m->blocked);
}

// Тест 3: один поток на массиве из одного элемента помечает его своим номером
// и затем сообщает, что продолжать невозможно (элемент уже занят)
TEST_F(MarkerTest, SingleMarkerMarksCellAndThenBlocks) {
    init(1, 1);
    startMarkers();

    ASSERT_TRUE(waitAllBlocked());

    EXPECT_EQ(snapshot(), vector<int>(1, 1));
    EXPECT_TRUE(markers[0]->blocked);
    EXPECT_NE(captured.str().find("Marker #1"), string::npos);
}

// Тест 4: после остановки всех потоков в массиве лежат только номера существующих потоков
TEST_F(MarkerTest, ArrayContainsOnlyValidMarkerIds) {
    const int k = 4;
    init(30, k);
    startMarkers();

    ASSERT_TRUE(waitAllBlocked());

    int nonZero = 0;
    for (int v : snapshot()) {
        EXPECT_GE(v, 0);
        EXPECT_LE(v, k);
        if (v != 0) ++nonZero;
    }
    EXPECT_GT(nonZero, 0);
}

// Тест 5: завершаемый поток зануляет только свои элементы, чужие не трогает
TEST_F(MarkerTest, TerminatedMarkerClearsOnlyItsOwnCells) {
    init(40, 3);
    startMarkers();
    ASSERT_TRUE(waitAllBlocked());

    vector<int> before = snapshot();

    terminate(2);

    vector<int> after = snapshot();
    ASSERT_EQ(before.size(), after.size());
    for (size_t i = 0; i < before.size(); ++i) {
        if (before[i] == 2)
            EXPECT_EQ(after[i], 0) << "ячейка " << i << " должна быть обнулена";
        else
            EXPECT_EQ(after[i], before[i]) << "ячейка " << i << " не должна меняться";
    }
    EXPECT_TRUE(markers[1]->finished);
    EXPECT_FALSE(markers[0]->finished);
    EXPECT_FALSE(markers[2]->finished);
}

// Тест 6: по сигналу «продолжить» поток возобновляет работу и снова блокируется
TEST_F(MarkerTest, MarkerResumesAfterContinueSignal) {
    init(1, 1);
    startMarkers();
    ASSERT_TRUE(waitAllBlocked());

    continueAll();                       // blocked сброшен, поток обязан проснуться

    ASSERT_TRUE(waitAllBlocked());       // и снова упереться в занятый элемент
    EXPECT_EQ(snapshot(), vector<int>(1, 1));

    // поток вывел сообщение дважды: до и после сигнала продолжения
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
        terminate(id);
        EXPECT_TRUE(markers[id - 1]->finished);

        // после завершения потока его номера в массиве быть не должно
        for (int v : snapshot())
            EXPECT_NE(v, id);

        continueAll();
    }

    EXPECT_EQ(snapshot(), vector<int>(n, 0));
}