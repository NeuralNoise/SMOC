#ifndef __SMOC_GRADER_CONFIG_H__
#define __SMOC_GRADER_CONFIG_H__

#include <string>
#include <vector>
#include "Parser.h"

using namespace std;

class CConfig {
public:
    CConfig();
    virtual ~CConfig();

    int LoadConfig();
    int GetProblemIndex(string problem);
    const char* GetProblemName(int index);
    int GetGradeNum(int index);
    int GetTimeLimit(int index);
    int GetType(int index);
    const char* GetLink(int index);
    const char* GetIOFile(int index);
    const char* GetGradeIOFile(int index);
    int GetMemLimit(int index);
    int GetOutputLimit(int index);
    const char* GetVersion();

    string server_address();

private:
    string version_;
    string server_address_;

    vector<string> languages_;

    int mCompileLimit; //sec
    int mStdoutLimit; //byte
    int mStderrLimit; //byte
    int mDisplayLimit; //byte

    CParser mParser;
};

#endif  // __SMOC_GRADER_CONFIG_H__
