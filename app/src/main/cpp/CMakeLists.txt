cmake_minimum_required(VERSION 3.22.1)

project("powersync_jni_demo")

add_library(${CMAKE_PROJECT_NAME} SHARED
        cpp-adapter.cpp
        JNIEnv.cpp
        PowerSyncDB.cpp
        PowerSyncSchema.cpp
        PowerSyncConnector.cpp
        utils.cpp
        OkHttp.cpp
)

target_link_libraries(
        ${CMAKE_PROJECT_NAME}
        android
        log
)
