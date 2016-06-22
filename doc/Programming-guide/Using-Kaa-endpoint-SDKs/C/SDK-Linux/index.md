---
layout: page
title: Linux
permalink: /:path/
nav: /:path/Programming-guide/Using-Kaa-endpoint-SDKs/C/SDK-Linux
sort_idx: 10
---
{% assign root_url = page.url | split: '/'%}
{% capture root_url  %} /{{root_url[1]}}/{{root_url[2]}}/{% endcapture %}

* TOC
{:toc}

This guide describes how to use *C SDK* on linux-based systems.

**Verified against:**

**Host OS:** Ubuntu 14.04 LTS Desktop 64-bit.

## C SDK build

This section describes how to build C SDK on linux for host.

### Dependencies

To build C SDK make sure that the following components are installed:

 - CMake (minimum required version is 2.8.12)
 - C99 compatible compiler (e.g. GCC)
 - OpenSSL

To install dependencies on Ubuntu execute the following command:

```
sudo apt-get install cmake libssl-dev gcc
```

### Build procedure

After you generated archive with C SDK, proceed as follows:

1. Unpack sdk:

        tar -xvf c-sdk-archive-name.tar.gz

2. Create the directory where sdk will be built:


        mkdir build
        cd build

3. Configure the build via cmake:

        cmake ..

4. Perform build:

        make

## Building Kaa application

This section is about how to build your Kaa aplication using C SDK.

In this guide we use *CMake* for the application build, so focus on the writing the build system rather than the application code. If you want master writing applociation code go to the [Programming guide]({{root_url}}/Programming-guide) section. Before continuing, make sure that all [dependencies](#dependencies) are installed.

1. The directories determine the build structure. To keep it clean and transparent, let's create the following directories:

    - `my-kaa-application` -- a root directory, which contains all files;
    - `my-kaa-application/kaa` -- for the C SDK source files;
    - `my-kaa-application/src` -- for the application code.


            mkdir my-kaa-application
            mkdir my-kaa-application/kaa
            mkdir my-kaa-application/src
            cd my-kaa-application

2. Generate C SDK and unpack it to `my-kaa-application/kaa` directory:

        tar -xvf c-sdk-archive.tar.gz -C my-kaa-application/kaa

3. Create the application code. Create `kaa-application.c` file in `my-kaa-application/src` directory:

        touch src/kaa-application.c

    Open `kaa-application.c` and write the application code. The code is simple: application just prints the string *"Hello, I am a Kaa Application!\n"*.

        #include <stdio.h>
        #include <stdlib.h>
        #include <kaa/kaa.h>
        #include <kaa/platform/kaa_client.h>
        #include <kaa/kaa_error.h>


        static void dummy_function(void *context)
        {
            printf("Hello, I am a Kaa Application!\n");
            kaa_client_stop(context);
        }

        int main(void)
        {
            kaa_client_t *kaa_client = NULL;
            kaa_error_t error = kaa_client_create(&kaa_client, NULL);
            if (error) {
                return EXIT_FAILURE;
            }

            error = kaa_client_start(kaa_client, dummy_function, (void *)kaa_client, 0);

            kaa_client_destroy(kaa_client);

            if (error) {
                return EXIT_FAILURE;
            }

            return EXIT_SUCCESS;
        }

4. Create `CMakeLists.txt` file in the `my-kaa-application` directory. It is a top-level cmake file which is responsible for the application build.

        touch CMakeLists.txt

    Add the following code to top-level `CMakeLists.txt`.The first line sets the minimum cmake version required to build kaa-application project. The second line sets project name and the language of the project:

        cmake_minimum_required(VERSION 3.5.2)
        project(kaa-application C)

    OpenSSL is required to build the application, so make sure that [it is installed](#dependencies).

        find_package(OpenSSL REQUIRED)

    Some compilers flags are set and C SDK project is included as a subproject to the kaa-application project.
    This is a clue. The parameter contains a path to the C SDK's `CMakeLists.txt`.

        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -g -Wall -Wextra")

        add_subdirectory(kaa)

    Compile executable named `kaa-app` using `src/kaa-application.c`, and link it with `kaac` and `crypto`.

        add_executable(kaa-app src/kaa-application.c)
        target_link_libraries(kaa-app kaac crypto)

    Full `CMakeLists.txt` code:

        cmake_minimum_required(VERSION 3.5.2)
        project(kaa-application C)

        find_package(OpenSSL REQUIRED)

        set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -std=c99 -g -Wall -Wextra")

        add_subdirectory(kaa)

        add_executable(kaa-app src/kaa-application.c)
        target_link_libraries(kaa-app kaac crypto)

5. Now your directory structure should look like this:

         - my-kaa-application
         - CMakeLists.txt
           - kaa
             - Unpacked C SDK
           - src
             - kaa-application.c

8. Finally we can build the application. The procedure should have already become familiar to you. Firstly create the directory where the build is performed:

        mkdir build
        cd build

6. Configure the build via CMake and run make.

        cmake -DKAA_MAX_LOG_LEVEL=3 ..
        make

    `KAA_MAX_LOG_LEVEL` [parameter]({{root_url}}/Programming-guide/Using-Kaa-endpoint-SDKs/C) is used here to decrease log level which is set by default to eliminate output pollution. Now run your application. Output should be the following one:

        Hello, I am a Kaa Application!

The full source codes could be found [here](https://github.com/kaaproject/kaa/pull/772/commits/9fae5d8a522b307bbe9f783f69604f2701ca60cc).
