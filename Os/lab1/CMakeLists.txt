cmake_minimum_required(VERSION 3.20)

project(LAB1 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
foreach(OUTPUTCONFIG ${CMAKE_CONFIGURATION_TYPES})
    string(TOUPPER ${OUTPUTCONFIG} OUTPUTCONFIG)
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${OUTPUTCONFIG} ${CMAKE_BINARY_DIR}/bin)
endforeach()

if(MSVC)
   add_compile_options(/utf-8)
endif()

add_executable(Main    main.cpp)
add_executable(Creator creator.cpp)
add_executable(Reporter reporter.cpp)

add_dependencies(Main Creator Reporter)