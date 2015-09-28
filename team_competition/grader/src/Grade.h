//
// Copyright 2002 by HM Research Ltd. All rights reserved.
//
// Grade.h: interface for the CGrade class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_)
#define AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
#include <sys/types.h>

#include "Parser.h"
#include "Config.h"

class CGrade {
public:
    CGrade();
    ~CGrade();

    int Compile(
            const string source_file,
            const string language,
            const string link_file,
            const string output_file,
            double* time);
    int Execute(
            const string language,
            const string task_name,
            const string input_file,
            const string output_file,
            const string error_file,
            int flag,
            int time_limit,
            int mem_limit,
            int output_limit,
            double* time,
            int* signal,
            int* exit_code);
    int ProcessSubmit(
            const string path,
            const string filename,
            const string & task,
            const string & language,
            char* result);
    int ProcessTest(
            const string path,
            const string filename,
            const string & task,
            const string & language,
            const string input_file,
            char* result);
    int ProcessGrade(
            const string path,
            const string filename,
            const string & task,
            const string & language,
            char* result);
    int SetConfig(CConfig* pConfig);
    int AppendFile(FILE* fp_write, const string read_file);

private:
    CParser* parser_;
    CConfig* config_;

    int AppendCompileResult(
            FILE* fp_write,
            const string compile_result,
            const string source_path,
            const string source_file);

    double CheckAnswer(const string input_file,
                       const string output_file,
                       const string answer_file,
                       const string checker,
                       int flag);
    int compileExe(const string source_file, const string output_file, double* time);
};

#endif // !defined(AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_)
