cmake_minimum_required(VERSION 3.22.1)

project("powersync_jni_demo")

file(DOWNLOAD "https://github.com/nlohmann/json/releases/download/v3.12.0/json.hpp" "${CMAKE_CURRENT_BINARY_DIR}/json/json.hpp" EXPECTED_HASH SHA256=aaf127c04cb31c406e5b04a63f1ae89369fccde6d8fa7cdda1ed4f32dfc5de63)

add_library(${CMAKE_PROJECT_NAME} SHARED
        cpp-adapter.cpp
        JNIEnv.cpp
        PowerSyncDB.cpp
        PowerSyncSchema.cpp
        PowerSyncConnector.cpp
        utils.cpp
        OkHttp.cpp
)

target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE "${CMAKE_CURRENT_BINARY_DIR}/json")

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
)
