//
// Copyright �� 2002 by HM Research Ltd. All rights reserved.
//

#include <time.h>
#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <stdarg.h>
#include <sys/stat.h>

#include "common.h"

FILE* fp_log = NULL;

int LOG(const char *format, ...)
{
	if (fp_log == NULL)
		return -1;

	time_t t;
	time(&t);
	struct tm* time;
	time = localtime(&t);

	fprintf(fp_log, "[%04d/%02d/%02d %02d:%02d:%02d]     ", time->tm_year+1900, time->tm_mon+1, time->tm_mday, time->tm_hour, time->tm_min, time->tm_sec);

	va_list args;
	va_start(args, format);

	vfprintf(fp_log, format, args);

	fprintf(fp_log, "\n");

	fflush(fp_log);
	
	return 0;
}

int GetFileSize(const string filename) {
	struct stat file_stat;

	if (stat(filename.c_str(), &file_stat) == -1) {
		//0820
		//LOG("file open error! [filename:%s] (%s)", filename, strerror(errno));
		return -1;
	}
	
	return file_stat.st_size;
}

int PrintFile(const string filename) {
	char buf[10240];
	
	FILE* fp = fopen(filename.c_str(), "r");
	
	if (fp == NULL) {
		LOG("file open error! [filename:%s]", filename.c_str());
		return -1;
	}
	
	LOG("[file : %s]", filename.c_str());
	
	while (fgets(buf, sizeof(buf), fp) != NULL)	{
		LOG("%s", buf);
	}
	fclose(fp);
	
	return 0;
}
