## Introduction

The main purpose of this project is to show how the PowerSync Kotlin SDK can be accessed from C++ on Android (NDK) using JNI. 
- This can be found in the `nativeApp` subdirectory.
- The main C++ entrypoint can found in: `cpp-adapter.cpp`

The project also includes a standard PowerSync implementation in Kotlin for comparison (`kotlinApp` subdirectory)

## Requirements
Start our [MongoDB self-host demo](https://github.com/powersync-ja/self-host-demo/) before running the demos in this project.

## Running the Demos
Open this repository in Android Studio to:
  * Launch `kotlinApp` (Kotlin only demo)
  * Launch `nativeApp` (C++ JNI demo)
