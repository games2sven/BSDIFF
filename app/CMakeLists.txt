
cmake_minimum_required(VERSION 3.4.1)


file(GLOB bzip_source src/main/cpp/bzip/*.c)

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/bspatch.c
             ${bzip_source})
include_directories(src/main/cpp/bzip)


find_library( # Sets the name of the path variable.
              log-lib
              log )


target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )