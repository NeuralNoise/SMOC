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

    int problem_count_;

    vector<string> problem_names_;
    vector<int> problem_types_; //1-normal, 2-output only, 3-library

    vector<int> number_of_tests_;
    vector<int> execution_time_limits_; //sec
    vector<int> memory_limits_;
    vector<int> output_limits_;
    vector<string> module_links_;
    vector<string> io_files_;
    vector<string> grade_io_files_;

    vector<string> languages_;

    int mCompileLimit; //sec
    int mStdoutLimit; //byte
    int mStderrLimit; //byte
    int mDisplayLimit; //byte

    CParser mParser;
};

#endif  // __SMOC_GRADER_CONFIG_H__
