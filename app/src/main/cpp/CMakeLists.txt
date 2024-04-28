cmake_minimum_required(VERSION 3.22.1)
project("rosjoydroid")

set(CMAKE_CXX_STANDARD 17)

find_package(fastdds-prefab)

file(GLOB_RECURSE FASTDDS_GEN "*.cxx")

add_library(${PROJECT_NAME} SHARED
        rosjoydroid.cpp
        ${FASTDDS_GEN})
target_include_directories(${PROJECT_NAME} PUBLIC .)
target_link_libraries(${PROJECT_NAME}
        fastdds-prefab::fastrtps
        fastdds-prefab::fastcdr)
