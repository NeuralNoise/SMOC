//
// Copyright 2002 by HM Research Ltd. All rights reserved.
//
// Grade.cpp: implementation of the CGrade class.
//
//////////////////////////////////////////////////////////////////////

#include "Grade.h"

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/resource.h>
#include <sys/wait.h>
#include <sys/time.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <signal.h>
#include <sstream>
#include <algorithm>

#include "common.h"

using namespace std;


CGrade::CGrade() :
    parser_(new CParser()) {

}

CGrade::~CGrade() {
    delete parser_;
}

int CGrade::compileExe(const string source_file, const string output_file, double* time) {
    struct stat file_stat;
    if (stat(output_file.c_str(), &file_stat) == 0) {
        int res = unlink(output_file.c_str());

        if (res == -1) {
            LOG("ERROR unlink (%s)", strerror(errno));
            return -3;
        }
    }
    stringstream ss;
    ss << "cp "<< source_file << " "<< output_file;
    system(ss.str().c_str());
    *time = 1;
    return 0;
}

/*----------------
 // return value
 //	0 : OK
 // -1 : compile error
 // -2 : compile time over
 //----------------*/
int CGrade::Compile(
        const string source_file,
        const string language,
        const string link_file,
        const string output_file,
        double* time) {

    if (language != LANG_PASCAL &&language != LANG_C &&language != LANG_CPP) {
        LOG("No such language");
        return -4;
    }

    struct stat file_stat;

    if (language == LANG_EXE) {
        return compileExe(source_file, output_file, time);
    }

    if (language == LANG_JAVA) {
        string command = source_file + ".java";
        if (stat(command.c_str(), &file_stat) == 0) {
            int res = unlink(command.c_str());
            if (res == -1) {
                LOG("ERROR unlink (%s)", strerror(errno));
                return -3;
            }
        }
        command = "cp " + source_file + " "+ source_file + ".java";
        system(command.c_str());
    }

    pid_t pid;
    double msec;
    struct rusage rusage;

    if (stat(output_file.c_str(), &file_stat) == 0) {
        int res = unlink(output_file.c_str());

        if (res == -1) {
            LOG("ERROR unlink (%s)", strerror(errno));
            return -3;
        }
    }
    if (language == LANG_JAVA) {
        string command = "touch " + output_file;
        system(command.c_str());
        system("rm -f box/*.class");
    }

    if ((pid = fork()) < 0) {
        printf("fork error\n");
    } else if (pid == 0) {
        int output = open(COMPILE_RESULT, O_WRONLY | O_CREAT | O_TRUNC, S_IRWXU);

        if (output == -1) {
            LOG("output file open error (%s)", strerror(errno));
            exit(1);
        }

        struct rlimit r;
        r.rlim_max = r.rlim_cur = 30;

        setrlimit(RLIMIT_CPU, &r);
        /*
         r.rlim_cur = r.rlim_max = 1*1024*1024;
         if (setrlimit(RLIMIT_FSIZE, &r) == -1)
         {
         LOG("setrlimt ERROR (file size) (%s)", strerror(errno));
         exit(1);
         }
         */
        stringstream ss;

        if (language != LANG_PASCAL){
            if (dup2(output, STDERR_FILENO) != STDERR_FILENO) {
                LOG("dup2 error to stderr (%s)", strerror(errno));
                exit(1);
            }
        }
        else {
            if (dup2(output, STDOUT_FILENO) != STDOUT_FILENO) {
                LOG("dup2 error to stderr (%s)", strerror(errno));
                exit(1);
            }
        }

        if (language == LANG_PASCAL) {
            ss << "-o" << output_file.c_str();
            if (link_file.empty()) {
                if (execlp("fpc",
                           "fpc",
                           "-O2",
                           "-XS",
                           "-Sg",
                           "-FEbox",
                           ss.str().c_str(),
                           source_file.c_str(),
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            } else {
                char linkage[100];
                sprintf(linkage, "-Fa%.*s", link_file.size()-2, link_file.c_str()+4); // HACK - delete .o from file

                if (execlp("fpc",
                           "fpc",
                           "-O2",
                           "-XS",
                           "-Sg",
                           "-Fulib/pas",
                           linkage,
                           "-FEbox",
                           ss.str().c_str(),
                           source_file.c_str(),
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            }
        } else if (language == LANG_C) {
            if (link_file.empty()) {
                if (execlp("gcc",
                           "gcc",
                           "-o", output_file.c_str(),
                           "-std=gnu99",
                           "-O2",
                           "-s",
                           "-static",
                           "-lm",
                           "-x", "c",
                           source_file.c_str(),
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            } else {
                if (execlp("gcc",
                           "gcc",
                           "-o", output_file.c_str(),
                           "-std=gnu99",
                           "-O2",
                           "-s",
                           "-static",
                           "-lm",
                           link_file.c_str(),
                           "-x", "c",
                           source_file.c_str(),
                           "-Ilib/c",
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            }
        } else if (language == LANG_CPP) {
            if (link_file.empty()) {
                if (execlp("g++",
                           "g++",
                           "-o", output_file.c_str(),
                           "-O2",
                           "-s",
                           "-static",
                           "-lm",
                           "-x", "c++",
                           source_file.c_str(),
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            } else {
                if (execlp("g++",
                           "g++",
                           "-o", output_file.c_str(),
                           "-std=gnu99",
                           "-O2",
                           "-s",
                           "-static",
                           "-lm",
                           link_file.c_str(),
                           "-x", "c++",
                           source_file.c_str(),
                           "-Ilib/c",
                           NULL) < 0) {
                    LOG("execlp ERROR(compile) (%s)", strerror(errno));
                    exit(1);
                }
            }
        } else {
            // no language
            exit(1);
        }
        exit(0);
    }

    int status;

    if (wait4(pid, &status, 0, &rusage) < 0)
    {
        LOG("wait error");
    }

    int sig = 0;
    if (WIFSIGNALED(status))
    sig = WTERMSIG(status);

    int exit_status = WEXITSTATUS(status);

    LOG("signal : %d, exit : %d (in compile)", sig, exit_status);

    if (language == LANG_JAVA && exit_status != 0) {
        unlink(output_file.c_str());
    }

    msec = (rusage.ru_utime.tv_sec + rusage.ru_stime.tv_sec) *1000 + (rusage.ru_utime.tv_usec + rusage.ru_stime.tv_usec) / 1000;

    if (msec > 30*1000) {
        LOG("compile tl exceeded");
        return -2;
    }

    if (stat(output_file.c_str(), &file_stat) < 0) {
        if (errno == ENOENT) {
            LOG("compile FAIL");
        }
        return -1;
    }

    *time = msec;

    return 0;
}


int CGrade::ProcessSubmit(const string path,
						  const string filename,
						  const string & task,
						  const string & language,
						  char* result) {
    string submit_result = TEMP_PATH + RESULT_FILE;
    string pathname = path + filename;

    FILE* fp_submit = fopen(submit_result.c_str(), "w+");

    if (fp_submit == NULL) {
        LOG("file open error [%s]", submit_result.c_str());
        return -1;
    }

    int res;

    //check problem name
    LOG("Header info: %s %s\n", task.c_str(), language.c_str());

    int problem_index = config_->GetProblemIndex(task);

    LOG("problem index : %d", problem_index);
    if (problem_index < 0)
    {
        sprintf(result, "RESULT SUBMIT FAIL na\n");
        fprintf(fp_submit, "[HEADER CHECK - ERROR]\n  problem name is invalid\n");

        fprintf(fp_submit, MSG_SUBMIT_FAIL);
        fclose(fp_submit);

        return 0;
    }

    int type = config_->GetType(problem_index);
    LOG("[SUBMIT]type:%d", type);
    if (type == PROBLEM_TYPE_STANDARD || type == PROBLEM_TYPE_MODULE)
    {
        fprintf(fp_submit, "[HEADER CHECK - OK]\n  task : %s\n  language : %s\n", task.c_str(), language.c_str());

        //compile
        double compile_time, execute_time;

        if (type == PROBLEM_TYPE_STANDARD) {
            res = this->Compile(pathname, language, "", USER_PROGRAM, &compile_time);
        } else if (type == PROBLEM_TYPE_MODULE) {
            res = this->Compile(pathname, language, "lib/unbuffered_stdout.o", USER_PROGRAM, &compile_time);
        } else { // never goes here
            res = this->Compile(pathname, language, config_->GetLink(problem_index), USER_PROGRAM, &compile_time);
        }

        if (res < 0) {
            sprintf(result, "RESULT SUBMIT FAIL %s\n", task.c_str());

            fprintf(fp_submit, "[COMPILE - ERROR]\n");

            //0819
            if (res == -1) {
                fprintf(fp_submit, "--[compiler message]------------\n");
                AppendCompileResult(fp_submit, COMPILE_RESULT, pathname.c_str(), filename);
            } else if (res == -2) {
                fprintf(fp_submit, "  compile time limit exceeded!\n");
            }

            fprintf(fp_submit, MSG_SUBMIT_FAIL);
            fclose(fp_submit);

            return 0;
        }
        fprintf(fp_submit, "[COMPILE - OK]\n  compile time : %5.2f seconds\n", compile_time/1000);
        /*
         struct stat file_stat;

         if (stat(COMPILE_RESULT, &file_stat) == 0 && file_stat.st_size > 0)
         {
         fprintf(fp_submit, "--[compiler message]------------\n");
         AppendCompileResult(fp_submit, COMPILE_RESULT, pathname, filename);
         }
         */
        //execute
        char input_file[256], output_file[256], answer_file[256];
        sprintf(input_file, "data/%s.%.*d.in",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, 0);
        sprintf(output_file, EXECUTE_RESULT);
        sprintf(answer_file, "data/%s.%.*d.sol",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, 0);

        if (type == PROBLEM_TYPE_STANDARD) {
            res = this->Execute(language, config_->GetProblemName(problem_index), input_file, output_file, EXECUTE_STDERR, 0, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, NULL, NULL);
        } else { // (type == PROBLEM_TYPE_MODULE)
            res = this->Execute(language, config_->GetProblemName(problem_index), input_file, output_file, EXECUTE_STDERR, 1, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, NULL, NULL);
        }

        if (res < 0) {
            sprintf(result, "RESULT SUBMIT OK %s\n", task.c_str());
            fprintf(fp_submit, "[SAMPLE DATA TEST - ERROR !!!]\n");
            if (res == -1)
            {
                fprintf(fp_submit, "  execution time limit exceeded!\n");
            }
            else if (res == -2)
            {
                fprintf(fp_submit, "  execution error!\n");
            }
            else if (res == -3)
            {
                fprintf(fp_submit, "  execution error! (invalid memory reference)\n");
            }
            else if (res == -4)
            {
                fprintf(fp_submit, "  output size limit exceeded!\n");
            }
            else if (res == -5)
            {
                fprintf(fp_submit, "  exit code is non-zero\n");

                // append output of VM for Java and Win32 EXE
                if (language == LANG_JAVA || language == LANG_EXE) {
                    AppendFile(fp_submit, output_file);
                    AppendFile(fp_submit, EXECUTE_STDERR);
                }
            }
            else if (res == -6)
            {
                fprintf(fp_submit, "  system error\n");
            }
            else
            LOG("ERROR : Unknown return value");

            //fprintf(fp_submit, MSG_SUBMIT_FAIL);
            fprintf(fp_submit, MSG_SUBMIT_OK);
            fclose(fp_submit);
            return 0;
        }

        //TODO : check answer!
        char checker[1024];
        if (type == PROBLEM_TYPE_OUTPUT) {
            sprintf(checker, "checker/%s.format", config_->GetProblemName(problem_index));
        }
        else {
            sprintf(checker, "checker/%s.checker", config_->GetProblemName(problem_index));
        }

        double points = CheckAnswer(input_file, output_file, answer_file, checker, problem_index);

        if (points == 0) //wrong answer
        {
            sprintf(result, "RESULT SUBMIT OK %s\n", task.c_str());
            fprintf(fp_submit, "[SAMPLE DATA TEST - ERROR !!!]\n  execution time : %5.2f seconds", execute_time/1000);

            struct stat file_stat;

            if (stat(answer_file, &file_stat) != 0)
            {
                LOG("ERROR : stat error (%s)", strerror(errno));
            }
            if (file_stat.st_size != 0)
            {
                fprintf(fp_submit, "\n--[correct answer]-----------\n");
                AppendFile(fp_submit, answer_file);
                fprintf(fp_submit, "\n");
            }

            fprintf(fp_submit, "\n--[your answer]--------------\n");
            AppendFile(fp_submit, output_file);
            fprintf(fp_submit, "\n");

            //0818
            FILE* fp_checker = fopen(CHECK_RESULT, "r");
            if (fp_checker != NULL)
            {
                fprintf(fp_submit, "\n--[checker message]----------\n");
                char temp_buf[10240];
                fgets(temp_buf, sizeof(temp_buf), fp_checker);
                while (fgets(temp_buf, sizeof(temp_buf), fp_checker) != NULL)
                {
                    fwrite(temp_buf, sizeof(char), strlen(temp_buf), fp_submit);
                }
                fclose(fp_checker);
                fprintf(fp_submit, "\n");
            }
            //0818

            //fprintf(fp_submit, MSG_SUBMIT_FAIL);
            fprintf(fp_submit, MSG_SUBMIT_OK);
            fclose(fp_submit);
            return 0;
        }
        else if (points < 0)
        {
            sprintf(result, "RESULT SUBMIT OK %s\n", task.c_str());
            fprintf(fp_submit, "system error !!!\n");
            fprintf(fp_submit, MSG_SUBMIT_OK);
            fclose(fp_submit);
            return 0;
        }

        //submit success!
        sprintf(result, "RESULT SUBMIT OK %s\n", task.c_str());

        fprintf(fp_submit, "[SAMPLE DATA TEST - OK]\n  execution time : %5.2f seconds\n", execute_time/1000);

        FILE* fp_checker = fopen(CHECK_RESULT, "r");
        if (fp_checker != NULL)
        {
            fprintf(fp_submit, "\n--[checker message]----------\n");
            char temp_buf[10240];
            fgets(temp_buf, sizeof(temp_buf), fp_checker);
            while (fgets(temp_buf, sizeof(temp_buf), fp_checker) != NULL)
            {
                fwrite(temp_buf, sizeof(char), strlen(temp_buf), fp_submit);
            }
            fclose(fp_checker);
            fprintf(fp_submit, "\n");
        }

        fprintf(fp_submit, MSG_SUBMIT_OK);
        fclose(fp_submit);

    }
    else if (type == PROBLEM_TYPE_OUTPUT)
    {
        fprintf(fp_submit, "[HEADER CHECK - OK]\n");
        fprintf(fp_submit, "  task : %s\n", task.c_str());

        //format check
        char format[1024];
        sprintf(format, "checker/%s.format", config_->GetProblemName(problem_index));

        double points = CheckAnswer("no_meaning", pathname, "no_meaning", format, problem_index);

        if (points == 0) //format error
        {
            sprintf(result, "RESULT SUBMIT FAIL %s\n", task.c_str());
            fprintf(fp_submit, "[FORMAT CHECK - ERROR]\n");

            FILE* fp_checker = fopen(CHECK_RESULT, "r");
            if (fp_checker != NULL)
            {
                fprintf(fp_submit, "\n--[checker message]----------\n");
                char temp_buf[10240];
                fgets(temp_buf, sizeof(temp_buf), fp_checker);
                while (fgets(temp_buf, sizeof(temp_buf), fp_checker) != NULL)
                {
                    fwrite(temp_buf, sizeof(char), strlen(temp_buf), fp_submit);
                }
                fclose(fp_checker);
                fprintf(fp_submit, "\n");
            }

            fprintf(fp_submit, MSG_SUBMIT_FAIL);
            fclose(fp_submit);

            return 0;
        }

        sprintf(result, "RESULT SUBMIT OK %s\n", task.c_str());
        fprintf(fp_submit, "[FORMAT CHECK - OK]\n");

        FILE* fp_checker = fopen(CHECK_RESULT, "r");
        if (fp_checker != NULL)
        {
            fprintf(fp_submit, "\n--[checker message]----------\n");
            char temp_buf[10240];
            fgets(temp_buf, sizeof(temp_buf), fp_checker);
            while (fgets(temp_buf, sizeof(temp_buf), fp_checker) != NULL)
            {
                fwrite(temp_buf, sizeof(char), strlen(temp_buf), fp_submit);
            }
            fclose(fp_checker);
            fprintf(fp_submit, "\n");
        }

        fprintf(fp_submit, MSG_SUBMIT_OK);
        fclose(fp_submit);

    }

    return 0;
}

int CGrade::SetConfig(CConfig* pConfig) {
    if (pConfig == NULL)
        return -1;

    config_ = pConfig;

    return 0;
}

int CGrade::AppendFile(FILE* fp_write, const string read_file) {
    if (fp_write == NULL)
        return -1;

    FILE* fp = fopen(read_file.c_str(), "r");
    char buf[10240];

    if (fp == NULL) {
        LOG("file open error! (%s)\n", read_file.c_str());
        return -1;
    }

    while (fgets(buf, sizeof(buf), fp) != NULL)
    {
        fwrite(buf, sizeof(char), strlen(buf), fp_write);
    }
    fclose(fp);

    return 0;
}

int CGrade::AppendCompileResult(
        FILE* fp_write,
        const string compile_result,
        const string source_path,
        const string source_file) {
    FILE* fp = fopen(compile_result.c_str(), "r");
    char buf[10240];

    if (fp == NULL) {
        LOG("file open error! (%s)\n", compile_result.c_str());
        return -1;
    }

    while (fgets(buf, sizeof(buf), fp) != NULL) {
        if (strncasecmp(buf, source_file.c_str(), source_file.length()) == 0) {
            fwrite(buf + source_file.length(), sizeof(char), strlen(buf + source_file.length()), fp_write);
        }
        else if (strncasecmp(buf, source_path.c_str(), source_path.length()) == 0) {
            fwrite(buf + source_path.length(), sizeof(char), strlen(buf + source_path.length()), fp_write);
        } else {
            fwrite(buf, sizeof(char), strlen(buf), fp_write);
        }
    }
    fclose(fp);

    return 0;
}

int CGrade::ProcessTest(
        const string path,
        const string filename,
        const string & task,
        const string & language,
        const string input_file,
        char* result) {
    string res_pathname = TEMP_PATH + RESULT_FILE;

    FILE* fp_test = fopen(res_pathname.c_str(), "w+");

    string pathname = path + filename;

    if (fp_test == NULL) {
        LOG("file open error [%s]", res_pathname.c_str());
        return -1;
    }

    int res;

    //check problem name
    int problem_index = config_->GetProblemIndex(task);

    if (problem_index < 0)
    {
        sprintf(result, "RESULT TEST FAIL na\n");
        fprintf(fp_test, "[HEADER CHECK - ERROR]\n  problem name is invalid\n");
        fclose(fp_test);

        return 0;
    }

    int type = config_->GetType(problem_index);
    LOG("[TEST]type:%d", type);
    if (type == PROBLEM_TYPE_OUTPUT)
    {
        sprintf(result, "RESULT TEST FAIL na\n");
        fprintf(fp_test, " Output-only task needs no testing\n");
        fclose(fp_test);

        return 0;
    }

    fprintf(fp_test, "[HEADER CHECK - OK]\n  task : %s\n  language : %s\n", task.c_str(), language.c_str());

    //compile
    double compile_time, execute_time;

    if (type == PROBLEM_TYPE_STANDARD) {
        res = this->Compile(pathname, language, "", USER_PROGRAM, &compile_time);
    } else if (type == PROBLEM_TYPE_MODULE) {
        res = this->Compile(pathname, language, "lib/unbuffered_stdout.o", USER_PROGRAM, &compile_time);
    } else { // never goes here
        res = this->Compile(pathname, language, config_->GetLink(problem_index), USER_PROGRAM, &compile_time);
    }

    if (res < 0)
    {
        sprintf(result, "RESULT TEST FAIL %s\n", task.c_str());

        fprintf(fp_test, "[COMPILE - ERROR]\n");

        if (res == -1)
        {
            fprintf(fp_test, "--[compiler message]------------\n");
            AppendCompileResult(fp_test, COMPILE_RESULT, pathname, filename);
        }
        else if (res == -2)
        {
            fprintf(fp_test, "  compile time limit exceeded!\n");
        }

        fprintf(fp_test, MSG_SUBMIT_FAIL);
        fclose(fp_test);

        return 0;
    }
    fprintf(fp_test, "[COMPILE - OK]\n  compile time : %5.2f seconds\n", compile_time/1000);
    /*
     struct stat file_stat;

     if (stat(COMPILE_RESULT, &file_stat) == 0 && file_stat.st_size > 0)
     {
     fprintf(fp_test, "--[compiler message]------------\n");
     AppendCompileResult(fp_test, COMPILE_RESULT, pathname, filename);
     }
     */
    //execute

    if (type == PROBLEM_TYPE_STANDARD) {
        res = this->Execute(language, config_->GetProblemName(problem_index), TEST_DATA, EXECUTE_RESULT, EXECUTE_STDERR, 0, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, NULL, NULL);
    } else { // (type == PROBLEM_TYPE_MODULE)
        res = this->Execute(language, config_->GetProblemName(problem_index), TEST_DATA, EXECUTE_RESULT, EXECUTE_STDERR, 1, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, NULL, NULL);
    }

    if (res < 0)
    {
        sprintf(result, "RESULT TEST FAIL %s\n", task.c_str());
        fprintf(fp_test, "[EXECUTION - ERROR]\n");
        if (res == -1)
        {
            fprintf(fp_test, "  execution time limit exceeded!\n");
        }
        else if (res == -2)
        {
            fprintf(fp_test, "  execution error!\n");
        }
        else if (res == -3)
        {
            fprintf(fp_test, "  execution error! (invalid memory reference)\n");
        }
        else if (res == -4)
        {
            fprintf(fp_test, "  output size limit exceeded!\n");
        }
        else if (res == -5)
        {
            fprintf(fp_test, "  exit code is non-zero\n");
        }
        else if (res == -6)
        {
            fprintf(fp_test, "  system error\n");
        }
        else
        LOG("Unknown return value");

        fclose(fp_test);
        return 0;
    }

    //test success!
    sprintf(result, "RESULT TEST OK %s\n", task.c_str());

    fprintf(fp_test, "[EXECUTION - OK]\n  execution time : %5.2f seconds\n", execute_time/1000);
    fprintf(fp_test, "\n--[your output]--------------\n");
    AppendFile(fp_test, EXECUTE_RESULT);
    //fprintf(fp_test, "\n-----------------------------\n");
    fprintf(fp_test, "\n");

    fclose(fp_test);

    return 0;
}

int CGrade::ProcessGrade(
        const string path,
        const string filename,
        const string & task,
        const string & language,
        char* result) {

    string res_grade = TEMP_PATH + RESULT_FILE;

    FILE* fp_grade = fopen(res_grade.c_str(), "w+");
    FILE* fp_log = fopen(GRADE_LOG, "w+");
    FILE* fp_list = fopen(RESULT_FILE_LIST, "w+");

    string pathname = path + filename;

    if (fp_grade == NULL) {
        LOG("file open error [%s]", res_grade.c_str());
        return -1;
    }
    if (fp_log == NULL)
    {
        LOG("file open error [%s]", GRADE_LOG);
        return -1;
    }
    if (fp_list == NULL)
    {
        LOG("file open error [%s]", RESULT_FILE_LIST);
        return -1;
    }

    int res;

    //check problem name
    //check language

    int problem_index = config_->GetProblemIndex(task);

    if (problem_index < 0)
    {
        sprintf(result, "RESULT GRADE FAIL na\n");
        fprintf(fp_grade, "problem name is invalid\n");
        fprintf(fp_log, "problem name is invalid\n");
        fclose(fp_grade);
        fclose(fp_log);
        fclose(fp_list);
        return 0;
    }
    int type = config_->GetType(problem_index);

    char command[1024];

    if (type == PROBLEM_TYPE_STANDARD || type == PROBLEM_TYPE_MODULE)
    {
        //compile
        double compile_time, execute_time;

        if (type == PROBLEM_TYPE_STANDARD) {
            res = this->Compile(pathname, language, "", USER_PROGRAM, &compile_time);
        } else if (type == PROBLEM_TYPE_MODULE) {
            res = this->Compile(pathname, language, "lib/unbuffered_stdout.o", USER_PROGRAM, &compile_time);
        } else { // never goes here
            res = this->Compile(pathname, language, config_->GetLink(problem_index), USER_PROGRAM, &compile_time);
        }

        if (res < 0)
        {
            sprintf(result, "RESULT GRADE FAIL %s\n", config_->GetProblemName(problem_index));

            fprintf(fp_grade, "compile error\n");
            fprintf(fp_log, "compile error\n");

            //0819
            if (res == -1)
            {
                fprintf(fp_grade, "--[compiler message]------------\n");
                AppendCompileResult(fp_grade, COMPILE_RESULT, pathname, filename);
            }
            else if (res == -2)
            {
                fprintf(fp_grade, "  compile time limit exceeded!\n");
                fprintf(fp_log, "compile time limit exceeded!\n");
            }

            fclose(fp_grade);
            fclose(fp_log);
            fclose(fp_list);

            return 0;
        }

        sprintf(result, "RESULT GRADE OK %s\n", task.c_str());
        fprintf(fp_grade, "%s,%s,%d,%d,", config_->GetProblemName(problem_index), language.c_str(), (int)compile_time, config_->GetGradeNum(problem_index));
        fprintf(fp_log, "task : %s\nlanguage : %s\ncompile time : %d\ndata_num : %d\n", config_->GetProblemName(problem_index), language.c_str(), (int)compile_time, config_->GetGradeNum(problem_index));

        int exit_signal;
        int exit_code;

        sprintf(command, "cp %s result/%s.compiler.out", COMPILE_RESULT, config_->GetProblemName(problem_index));
        system(command);
        fprintf(fp_list, "%s.compiler.out\n", config_->GetProblemName(problem_index));

        sprintf(command, "cp %s result/%s.bin", USER_PROGRAM, config_->GetProblemName(problem_index));
        system(command);
        fprintf(fp_list, "%s.bin\n", config_->GetProblemName(problem_index));

        for (int i=1; i<=config_->GetGradeNum(problem_index); i++)
        {
            char input_file[256], output_file[256], answer_file[256];

            sprintf(input_file, "data/%s.%.*d.in",
                                    config_->GetProblemName(problem_index),
                                    TESTCASE_PADDING_LENGTH, i);
            sprintf(output_file, EXECUTE_RESULT);
            sprintf(answer_file, "data/%s.%.*d.sol",
                                    config_->GetProblemName(problem_index),
                                    TESTCASE_PADDING_LENGTH, i);

            if (type == PROBLEM_TYPE_STANDARD) {
                res = this->Execute(language, config_->GetProblemName(problem_index), input_file, output_file, EXECUTE_STDERR, 0, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, &exit_signal, &exit_code);
            } else { // (type == PROBLEM_TYPE_STANDARD)
                res = this->Execute(language, config_->GetProblemName(problem_index), input_file, output_file, EXECUTE_STDERR, 2, config_->GetTimeLimit(problem_index), config_->GetMemLimit(problem_index), config_->GetOutputLimit(problem_index), &execute_time, &exit_signal, &exit_code);
            }

            fprintf(fp_log, "input : %s, output :%s, sol : %s\n", input_file, output_file, answer_file);
            fprintf(fp_log, "signal : %d, exit_code : %d\n", exit_signal, exit_code);
            fprintf(fp_log, "Execute return value : %d\n", res);

            fprintf(fp_grade, "%d,", i);

            if (res == -1)
            {
                fprintf(fp_grade, "-1,t,-1,-1,");
            }
            else if (res == -2)
            {
                fprintf(fp_grade, "-1,e,-1,-1,");
            }
            else if (res == -3)
            {
                fprintf(fp_grade, "-1,e,-1,-1,");
            }
            else if (res == -4)
            {
                fprintf(fp_grade, "-1,x,-1,-1,");
            }
            else if (res == -5)
            {
                fprintf(fp_grade, "-1,e,-1,-1,");
            }
            else if (res == -6)
            {
                fprintf(fp_grade, "-1,s,-1,-1,");
            }
            else
            {
                fprintf(fp_grade, "%lf,", execute_time);
                fprintf(fp_log, "execution time : %lf\n", execute_time);

                //TODO : check answer!
                LOG("CHECK START");
                char checker[1024];
                sprintf(checker, "checker/%s.checker", config_->GetProblemName(problem_index));

                double points = CheckAnswer(input_file, output_file, answer_file, checker, problem_index);

                if (points <= 0)
                fprintf(fp_grade, "x,%d,%d,", exit_signal, exit_code);
                else
                fprintf(fp_grade, "%.9lg,%d,%d,", points, exit_signal, exit_code);

                sprintf(command, "cp %s result/%s.%.*d.out",
                                        EXECUTE_RESULT, config_->GetProblemName(problem_index),
                                        TESTCASE_PADDING_LENGTH, i);
                system(command);
                fprintf(fp_list, "%s.%.*d.out\n", task.c_str(), TESTCASE_PADDING_LENGTH, i);

                sprintf(command, "cp %s result/%s.%.*d.err",
                                        EXECUTE_STDERR, config_->GetProblemName(problem_index),
                                        TESTCASE_PADDING_LENGTH, i);
                system(command);
                fprintf(fp_list, "%s.%.*d.err\n", task.c_str(), TESTCASE_PADDING_LENGTH, i);

                sprintf(command, "cp %s result/%s.%.*d.checker.out",
                                        CHECK_RESULT, task.c_str(),
                                        TESTCASE_PADDING_LENGTH, i);
                system(command);
                fprintf(fp_list, "%s.%.*d.checker.out\n", task.c_str(), TESTCASE_PADDING_LENGTH, i);
            }

            fprintf(fp_grade, "0,");
        }

        fclose(fp_grade);
        fclose(fp_log);
        fclose(fp_list);
    }
    else if (type == PROBLEM_TYPE_OUTPUT)
    {
        //checker

        char input_file[256];
        char sol_file[256];
        int x_index = atoi(task.c_str() + strlen(config_->GetProblemName(problem_index)));

        //success
        sprintf(result, "RESULT GRADE OK %s%.*d\n",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);

        fprintf(fp_grade, "%s%.*d,NA,NA,1,",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);

        sprintf(input_file, "data/%s.%.*d.in",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);
        sprintf(sol_file, "data/%s.%.*d.sol",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);
        char checker[1024];
        sprintf(checker, "checker/%s.checker", config_->GetProblemName(problem_index));

        double points = CheckAnswer(input_file, pathname, sol_file, checker, problem_index);

        fprintf(fp_grade, "1,0,");
        if (points <= 0)
        {
            fprintf(fp_grade, "x,0,0,0,");
        }
        else
        {
            fprintf(fp_grade, "%.9lg,0,0,0,", points);
        }

        sprintf(command, "cp %s result/%s.%.*d.checker.out",
                                CHECK_RESULT, config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);
        system(command);
        fprintf(fp_list, "%s.%.*d.checker.out\n",
                                config_->GetProblemName(problem_index),
                                TESTCASE_PADDING_LENGTH, x_index);

        fclose(fp_grade);
        fclose(fp_log);
        fclose(fp_list);
    }

    return 0;
}

double CGrade::CheckAnswer(
        const string input_file,
        const string output_file,
        const string answer_file,
        const string checker,
        int flag) {

    LOG("[CHECK ANSWER]input:%s, output:%s, answer:%s",
            input_file.c_str(),
            output_file.c_str(),
            answer_file.c_str());

    pid_t pid;

    if ((pid = fork()) < 0) {
        LOG("fork error");
    } else if (pid == 0) {
        int output = open(CHECK_RESULT, O_WRONLY | O_CREAT | O_TRUNC, S_IRWXU);

        if (output == -1) {
            LOG("%s file open error (%s)", CHECK_RESULT, strerror(errno));
            exit(1);
        }

        if (dup2(output, STDOUT_FILENO) != STDOUT_FILENO) {
            LOG("dup2 error to stdout (%s)", strerror(errno));
            exit(1);
        }

        if (execl(checker.c_str(),
                checker.c_str(),
                input_file.c_str(),
                output_file.c_str(),
                answer_file.c_str(),
                NULL) < 0) {
            LOG("execlp ERROR(%s) (%s)", checker.c_str(), strerror(errno));
            exit(1);
        }
        string command = "rm " + output_file;
        system(command.c_str());

        exit(0);
    }

    int status;
    struct rusage rusage;
    if (wait4(pid, &status, 0, &rusage) < 0) {
        LOG("wait error");
    }

    FILE* fp = fopen(CHECK_RESULT, "r");
    char buf[10240];

    if (fgets(buf, sizeof(buf), fp) == NULL)
    {
        return -2;
    }

    fclose(fp);

    double points;
    sscanf(buf, "%lf", &points);

    fp = fopen(CHECK_RESULT, "r");

    while (fgets(buf, sizeof(buf), fp) != NULL)
    {
        LOG("[CHECKER OUTPUT:%s]", buf);
    }
    fclose(fp);

    return points;
}

/*----------------
 // return value
 //	0 : OK
 // -1 : execution time over
 // -2 : execute error
 // -3 : execute error (invalid memory reference)
 // -4 : output limit over
 // -5 : exit code is not zero
 // -6 : system error
 // -7 : executable type error
 //----------------*/
int CGrade::Execute(
        const string language,
        const string task_name,
        const string input_file,
        const string output_file,
        const string error_file,
        int flag, // 0: no module (standard) | 1: test module | 2: grade module
        int time_limit,
        int mem_limit,
        int output_limit,
        double* time,
        int* exit_signal,
        int* exit_code) {
    //Executable type: 0, 1, 2-Native excutable
    if (language != LANG_C && language != LANG_CPP && language != LANG_PASCAL) {
        LOG("Wrong executable type");
        return -7;
    }

    LOG("[Execute] input:%s, output:%s, limit:%d:%d:%d",
        input_file.c_str(),
        output_file.c_str(),
        time_limit,
        mem_limit,
        output_limit);

    char time_limit_s[20];      sprintf(time_limit_s, "%lf", time_limit/1000.0);
    char mem_limit_s[20];       sprintf(mem_limit_s, "%d", mem_limit*1024);
    char wall_limit_s[20];      sprintf(wall_limit_s, "%lf", max(3*time_limit/1000.0, 1.0)+10);
    char output_limit_s[20];    sprintf(output_limit_s, "%d", output_limit);

    int pipes[2][2];
    if (flag) {
        if (pipe(pipes[0]) == -1 ||
            pipe(pipes[1]) == -1) {
            LOG("pipe opening error");
            return -6;
        }
    }

    pid_t pid;

    if ((pid = fork()) < 0) {
        LOG("fork error");
        return -6;
    } else if (pid == 0) {

        int err = open(BOX_RESULT, O_WRONLY | O_CREAT | O_TRUNC);
        if (err == -1) {
            LOG("BOX_RESULT file open error (%s)", strerror(errno));
            exit(11);
        }

        if (dup2(err, STDERR_FILENO) != STDERR_FILENO) {
            LOG("dup2 error to stderr (%s)", strerror(errno));
            exit(11);
        }

        if (setuid(12345) == -1 || seteuid(12345) == -1) {
            LOG("setuid ERROR (%s)", strerror(errno));
            exit(11);
        }

        if (!flag) {
            for(int i=3;i<256;i++) close(i); // else might inherit some fd from grader

            if (execl("./sandbox", "./sandbox",
                      "-a", "2",
                      "-f", "-T",
                      "-i", input_file.c_str(),
                      "-o", output_file.c_str(),
                      "-r", error_file.c_str(),
                      "-O", output_limit_s,
                      "-m", mem_limit_s,
                      "-t", time_limit_s,
                      "-w", wall_limit_s,
                      "./box/tmp.exe", NULL) < 0) {
                LOG("execl ERROR(run) (%s)", strerror(errno));
                exit(11);
            }
        } else {
            if (dup2(pipes[0][0], 0) != 0 ||
                dup2(pipes[1][1], 1) != 1) {
                LOG("Error redirecting pipes (%s)", strerror(errno));
                exit(11);
            }

            for(int i=3;i<256;i++) close(i); // else might inherit some fd from grader

            if (execl("./sandbox", "./sandbox",
                      "-a", "2",
                      "-f", "-T", // -i, -o inherited
                      "-r", error_file.c_str(),
                      "-O", output_limit_s,
                      "-m", mem_limit_s,
                      "-t", time_limit_s,
                      "-w", wall_limit_s,
                      "./box/tmp.exe", NULL) < 0) {
                LOG("execl ERROR(run) (%s)", strerror(errno));
                exit(11);
            }
        }
    } else {
        int status;

        if (flag) {
            char module[1024];
            sprintf(module, "checker/%s.module%s", task_name.c_str(), (flag == 1) ? "_test" : "");

            pid_t module_pid;
            if ((module_pid = fork()) < 0) {
                LOG("fork error");
                return -6;
            } else if (module_pid == 0) {
                close(2);
                if (open("./tmp/pipe.module.stderr", O_WRONLY | O_CREAT | O_TRUNC, 0666) != 2) {
                    LOG("Unable to open ./tmp/pipe.module.stderr for writing (%s)", strerror(errno));
                    exit(11);
                }

                if (dup2(pipes[1][0], 0) != 0 ||
                    dup2(pipes[0][1], 1) != 1) {
                    LOG("Error redirecting pipes (%s)", strerror(errno));
                    exit(11);
                }
                for (int i=0;i<4;i++)
                    close(((int *)pipes)[i]);

                if (execl("./sandbox", "./sandbox",
                          "-w", wall_limit_s,
                          module, // -i -o inherited
                          input_file.c_str(),
                          output_file.c_str(),
                          NULL) < 0) {
                    LOG("execl ERROR(run) (%s)", strerror(errno));
                    exit(11);
                }
            }

            for (int i=0;i<4;i++)
                close(((int *)pipes)[i]);

            waitpid(module_pid, &status, 0);
            if (status == 11)
                return -6;
        }

        waitpid(pid, &status, 0);
        if (status == 11)
            return -6;

        int box_result = open(BOX_RESULT, O_RDONLY);
        if (box_result == -1) {
            LOG("BOX_RESULT file open error (%s)", strerror(errno));
            return -6;
        }
        char buff[1024]; memset(buff, 0, sizeof(buff));
        read(box_result, buff, 1024);
        close(box_result);
        LOG("BOX: %s\n", buff);

        int v1; double v2, v3;
        if (sscanf(buff, "OK (%lf sec real, %lf sec wall, %lf MB, %d syscalls)", time, &v3, &v2, &v1) == 4)
        {    *time = *time*1000;
            if (exit_signal != NULL)    *exit_signal    = 0;
            if (exit_code != NULL)      *exit_code      = 0;
            return 0;
        }
        if (strncmp(buff, "Time limit exceeded", 19) == 0)
        {   if (exit_signal != NULL)    *exit_signal    = 0;
            if (exit_code != NULL)      *exit_code      = 0;
            return -1;
        }
        if ((sscanf(buff, "Received signal %d", &v1) == 1) ||
            (sscanf(buff, "Committed suicide by signal %d", &v1) == 1) ||
            (sscanf(buff, "Caught fatal signal %d", &v1) == 1) ||
            ((strncmp(buff, "Interrupted", 11) == 0) && (v1=SIGINT)) || // it is v1= not ==
            ((strncmp(buff, "Breakpoint", 10) == 0) && (v1=SIGINT)) // it is v1= not ==
           )
        {   if (exit_signal != NULL)    *exit_signal    = v1;
            if (exit_code != NULL)      *exit_code      = 1;

            if (v1 == SIGXFSZ)
                return -4;
            else
                return -2;
        }
        if (sscanf(buff, "Exited with error status %d", &v1) == 1)
        {   if (exit_signal != NULL)    *exit_signal    = 0;
            if (exit_code != NULL)      *exit_code      = v1;
            return -5;
        }
        if (strncmp(buff, "File", 4)                                        != 0 ||
            strncmp(buff, "Access", 6)                                      != 0 ||
            strncmp(buff, "open(", 5)                                       != 0 ||
            strncmp(buff, "lseek64(mem): ", 14)                             != 0 ||
            strncmp(buff, "read(mem): ", 11)                                != 0 ||
            strncmp(buff, "Forbidden access to file ", 25)                  != 0 ||
            strncmp(buff, "read on /proc/", 14)                             != 0 ||
            strncmp(buff, "/proc/", 6)                                      != 0 ||
            strncmp(buff, "proc stat syntax error ", 23)                    != 0 ||
            strncmp(buff, "Unknown syscall", 15)                            != 0 ||
            strncmp(buff, "Syscall ", 8)                                    != 0 ||
            strncmp(buff, "FO: Forbidden syscall ", 22)                     != 0
            )
        {   if (exit_signal != NULL)    *exit_signal    = 0;
            if (exit_code != NULL)      *exit_code      = 0;
            return -2;
        }
    }
    return -6;
}
