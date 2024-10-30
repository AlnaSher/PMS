cmake_minimum_required(VERSION 3.4.1)

# Set the library name and specify the source files
add_library(native-lib SHARED src/main/cpp/native-lib.c)

# Find the log library
find_library(log-lib log)

# Link the log library to your native library
target_link_libraries(native-lib ${log-lib})