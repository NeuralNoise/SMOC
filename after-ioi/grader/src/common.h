//
// Copyright �� 2002 by HM Research Ltd. All rights reserved.
//

#if !defined(_YWCHOI_COMMON_H_)
#define _YWCHOI_COMMON_H_

#include <string>

using std::string;

int LOG(const char *format, ...);
int GetFileSize(const string filename);
int PrintFile(const string filename);


#define TEST_DATA "./tmp/test.data"

#define COMPILE_RESULT "./tmp/result.compile"
#define EXECUTE_RESULT "./tmp/result.execute"
#define EXECUTE_STDERR "./tmp/stderr.execute"
#define BOX_RESULT "./tmp/result.box"
#define CHECK_RESULT "./tmp/result.check"

#define USER_PROGRAM "./box/tmp.exe"


#define TEMP_PATH "./tmp/"
#define SOURCE_FILE "source"

#define RESULT_FILE_LIST "./result/list"
#define GRADE_LOG "./result/log"


#define LANG_C "C"
#define LANG_CPP "C++"
#define LANG_PASCAL "PASCAL"
#define LANG_CSHARP "C#"
#define LANG_JAVA "Java"
#define LANG_EXE "exe"

#define TESTCASE_PADDING_LENGTH 3

#define PROBLEM_TYPE_STANDARD 1
#define PROBLEM_TYPE_OUTPUT 2
#define PROBLEM_TYPE_MODULE 3


#endif // !defined(_YWCHOI_COMMON_H_)
