#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <unistd.h>

#include "Grader.h"
#include "common.h"

#include "grade.pb.h"

extern FILE* fp_log;
extern int errno;

int main(int argc, char** argv) {
	GOOGLE_PROTOBUF_VERIFY_VERSION;

	fp_log = fopen("grader.log", "a+");

	if (fp_log == NULL) {
		fprintf(stdout, "log file OPEN ERROR (%s)", strerror(errno));
		return -1;
	}

	LOG(".....START!");

	CGrader grader;

	if (grader.Init() < 0) {
		LOG("Init fail");
		return -1;
	}
	LOG("Init OK");

	while (true) {
		grader.Process();
		sleep(3);
	}

	return 0;
}
