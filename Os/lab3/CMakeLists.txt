cmake_minimum_required(VERSION 3.14)
project(marker_lab CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

if(MSVC)
  add_compile_options(/utf-8)   # исходники в UTF-8
endif()

# Сама лабораторная работа
add_executable(lab main.cpp marker.cpp)

# Google Test (скачивается автоматически при конфигурации)
include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/v1.14.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# Тесты
enable_testing()
add_executable(lab_tests tests.cpp marker.cpp)
target_link_libraries(lab_tests GTest::gtest_main)

include(GoogleTest)
gtest_discover_tests(lab_tests)