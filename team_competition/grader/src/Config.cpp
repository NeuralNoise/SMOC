//
// Copyright  2002 by HM Research Ltd. All rights reserved.
//
// Config.cpp: implementation of the CConfig class.
//
//////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <string>
#include <sstream>

#include "common.h"
#include "Config.h"
#include "Constants.h"

using namespace std;

CConfig::CConfig() {
}

CConfig::~CConfig() {
}

int CConfig::LoadConfig() {
    string buf;

    if (mParser.GetValue("CONFIG", "VERSION", 256, &version_) < 0) {
        LOG("can't find [CONFIG, VERSION]");
        return -1;
    }

    if (mParser.GetValue("SERVER", "ADDRESS", 256, &server_address_) < 0) {
        LOG("can't find [SERVER, ADDRESS]");
        return -1;
    }

    if (mParser.GetValue("PROBLEM", "NUM", 256, &buf) < 0) {
        LOG("can't find [PROBLEM, NUM]");
        return -1;
    }

    problem_count_ = atoi(buf.c_str());

    int res;
    char prob[256];

    for (int i = 0; i < problem_count_; ++i) {
        sprintf(prob, "PROBLEM%d", i + 1);
        res = mParser.GetValue(prob, "NAME", 256, &buf);
        problem_names_.push_back(buf);
        if (res < 0) {
            LOG("can't find [%s, NAME]", prob);
            return -1;
        }

        res = mParser.GetValue(prob, "TYPE", 256, &buf);
        if (res < 0) {
            LOG("can't find [%s, TYPE]", prob);
            return -1;
        }

        problem_types_.push_back(atoi(buf.c_str()));

        module_links_.push_back("");
        io_files_.push_back("");
        grade_io_files_.push_back("");

        res = mParser.GetValue(prob, "GRADE_NUM", 256, &buf);
        if (res < 0) {
            LOG("can't find [%s, GRADE_NUM]", prob);
            return -1;
        }

        number_of_tests_.push_back(atoi(buf.c_str()));

        res = mParser.GetValue(prob, "TIME_LIMIT", 256, &buf);
        if (res < 0) {
            LOG("can't find [%s, TIME_LIMIT]", prob);
            return -1;
        }

        execution_time_limits_.push_back(atoi(buf.c_str()));

        res = mParser.GetValue(prob, "MEM_LIMIT", 256, &buf);
        if (res < 0) {
            LOG("can't find [%s, MEM_LIMIT]", prob);
            return -1;
        }

        memory_limits_.push_back(atoi(buf.c_str()));

        res = mParser.GetValue(prob, "OUTPUT_LIMIT", 256, &buf);
        if (res < 0) {
            LOG("can't find [%s, OUTPUT_LIMIT]", prob);
            return -1;
        }

        output_limits_.push_back(atoi(buf.c_str()));

        LOG("PROBLEM %d [NAME:%s, TYPE:%d, TEST_NUM:%d, Time Limit:%d, Mem Limit:%d] ",
                i + 1,
                problem_names_[i].c_str(),
                problem_types_[i],
                number_of_tests_[i],
                execution_time_limits_[i],
                memory_limits_[i]);
    }

    if (mParser.GetValue("LANGUAGE", "NUM", 256, &buf) < 0) {
        LOG("can't find [LANGUAGE, NUM]");
        return -1;
    }

    LOG("server [%s:%d]", server_address_.c_str(), Constants::SERVER_PORT);
    return 0;
}

int CConfig::GetProblemIndex(string problem) {
    for (int i = 0; i < problem_count_; i++) {
        if ((problem_types_[i] == PROBLEM_TYPE_STANDARD) ||
            (problem_types_[i] == PROBLEM_TYPE_MODULE)) {
            if (problem == problem_names_[i]) {
                return i;
            }
        } else if (problem_types_[i] == PROBLEM_TYPE_OUTPUT) {
            if ((problem.length() == problem_names_[i].length() + TESTCASE_PADDING_LENGTH) &&
                    (strncasecmp(problem.c_str(),
                     problem_names_[i].c_str(),
                     problem_names_[i].length()) == 0)) {
                return i;
            }
        }
    }

    //TODO : language CHECK!
    return -1;
}

const char* CConfig::GetProblemName(int index) {
    return problem_names_[index].c_str();
}

int CConfig::GetGradeNum(int index) {
    return number_of_tests_[index];
}

int CConfig::GetTimeLimit(int index) {
    return execution_time_limits_[index];
}

int CConfig::GetType(int index) {
    return problem_types_[index];
}

const char* CConfig::GetLink(int index) {
    return module_links_[index].c_str();
}

const char* CConfig::GetIOFile(int index) {
    return io_files_[index].c_str();
}

const char* CConfig::GetGradeIOFile(int index) {
    return grade_io_files_[index].c_str();
}

int CConfig::GetMemLimit(int index) {
    return memory_limits_[index];
}

int CConfig::GetOutputLimit(int index) {
    return output_limits_[index];
}

const char* CConfig::GetVersion() {
    return version_.c_str();
}

string CConfig::server_address() {
    return server_address_;
}
