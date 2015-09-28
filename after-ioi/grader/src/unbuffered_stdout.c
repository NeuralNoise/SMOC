#include <stdio.h>

void unbuffer_stdout() __attribute__((constructor));;

void unbuffer_stdout() {
    setvbuf(stdout, NULL, _IOLBF, 0);
}
