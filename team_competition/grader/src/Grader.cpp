// Copyright  2002 by HM Research Ltd. All rights reserved.
//
// Grader.cpp: implementation of the CGrader class.
//
//////////////////////////////////////////////////////////////////////


#include <errno.h>
#include <string.h>
#include <stdlib.h>

#include "common.h"
#include "Constants.h"
#include "Grader.h"

CGrader::CGrader() :
    mComm(new CComm()), mConfig(new CConfig()), mGrade(new CGrade()), mParser(new CParser()) {
}

CGrader::~CGrader() {
    delete mComm;
    delete mConfig;
    delete mGrade;
    delete mParser;
}

int CGrader::Init() {
    int res = LoadConfig();

    if (res < 0)
        return -1;

    mGrade->SetConfig(mConfig);

    return 0;
}

void CGrader::InitDirectories() {
    system("umount box/proc 2> /dev/null");

    // for use with bind but just to be on the safe side
    system("umount box/lib 2> /dev/null");
    system("umount box/usr/lib 2> /dev/null");
    system("umount box/usr/local/mono 2> /dev/null");

    system("umount /usr/local/smoc/lib.iso 2> /dev/null");
    system("umount /usr/local/smoc/usr.iso 2> /dev/null");

    //rm any old directories & files
    system("rm -rf box");
    system("rm -rf result");
    system("rm -rf tmp");
    //mkdirs
    system("mkdir box");
    system("mkdir result");
    system("mkdir tmp");
    //chown
    if (system("chown 12345:12345 box") ||
        system("chown 12345:12345 result") ||
        system("chown 12345:12345 tmp")) {
        LOG("Unable to set permissions to box/result/tmp");
        exit(-1);
    }
}


int CGrader::LoadConfig() {
    //LoadConfig
    int res;

    res = mConfig->LoadConfig();

    if (res < 0) {
        LOG("Load Configuration fail");
        return -1;
    }

    return 0;
}

int CGrader::Process() {
    int res;

    res = mComm->Connect(mConfig->server_address().c_str(), Constants::SERVER_PORT, mConfig->GetVersion());

    if (res < 0)
        return -1;

    int command;
    char response[1024];

    string result = TEMP_PATH + RESULT_FILE;
    string task;
    string language;

    while (true) {
        InitDirectories();

        command = mComm->RecvJob(&task, &language);

        if (command == 1) //submit
        {
            res = mGrade->ProcessSubmit(TEMP_PATH, SOURCE_FILE, task, language, response);
            if (res < 0) {
                LOG("Error in ProcessSubmit");
                break;
            }

            if (mComm->SendMsg(response) < 0)
                break;
            if (mComm->SendInt(KEY) < 0)
                break;

            int file_size = GetFileSize(result);

            if (file_size > 10*1024) {
                file_size = 10*1024+strlen("\n...truncated!");
                if (mComm->SendInt(file_size) < 0)
                    break;
                if (mComm->SendFile(result, 10*1024) < 0)
                    break;
                if (mComm->SendMsg("\n...truncated!") < 0)
                    break;
            } else {
                if (mComm->SendInt(file_size) < 0)
                    break;
                if (mComm->SendFile(result) < 0)
                    break;
            }
            /*
             if (mComm.SendInt(file_size) < 0)
             break;
             if (mComm.SendFile(result) < 0)
             break;*/
            if (PrintFile(result) < 0)
                break;
        } else if (command == 2) {
            res = mGrade->ProcessTest(TEMP_PATH, SOURCE_FILE, task, language, TEST_DATA, response);
            if (res < 0) {
                LOG("Error in ProcessTest");
                break;
            }

            if (mComm->SendMsg(response) < 0)
                break;
            if (mComm->SendInt(KEY) < 0)
                break;

            int err_file_size = GetFileSize(EXECUTE_STDERR);
            if (err_file_size > 0) {
                FILE* fp = fopen(result.c_str(), "a");
                if (fp == NULL) {
                    LOG("file open error (%s)", strerror(errno));
                    break;
                }
                fprintf(fp, "--[stderr]-------------------\n");
                mGrade->AppendFile(fp, EXECUTE_STDERR);
                fprintf(fp, "\n");
                fclose(fp);
            }

            int file_size = GetFileSize(result);

            if (file_size > 10*1024) {
                file_size = 10*1024+strlen("\n...truncated!");
                if (mComm->SendInt(file_size) < 0)
                    break;
                if (mComm->SendFile(result, 10*1024) < 0)
                    break;
                if (mComm->SendMsg("\n...truncated!") < 0)
                    break;
            } else {
                if (mComm->SendInt(file_size) < 0)
                    break;
                if (mComm->SendFile(result) < 0)
                    break;
            }
            /*
             if (mComm->SendInt(file_size) < 0)
             break;
             if (mComm->SendFile(result) < 0)
             break;*/
            if (PrintFile(result) < 0) //0815
                break;
        } else if (command == 3) { //grade
            res = mGrade->ProcessGrade(TEMP_PATH, SOURCE_FILE, task, language, response);
            if (res < 0) {
                LOG("Error in ProcessGrade");
                break;
            }

            if (mComm->SendMsg(response) < 0)
                break;
            if (mComm->SendInt(KEY) < 0)
                break;
            int file_size = GetFileSize(result);
            if (file_size < 0)
                break;
            if (mComm->SendInt(file_size) < 0)
                break;
            if (mComm->SendFile(result) < 0)
                break;

            if (mComm->SendInt(KEY) < 0)
                break;
            file_size = GetFileSize(GRADE_LOG);
            if (file_size < 0)
                break;
            if (mComm->SendInt(file_size) < 0)
                break;
            if (mComm->SendFile(GRADE_LOG) < 0)
                break;

            if (mComm->SendInt(KEY) < 0)
                break;
            file_size = GetFileSize(RESULT_FILE_LIST);
            if (file_size < 0)
                break;
            if (mComm->SendInt(file_size) < 0)
                break;
            if (mComm->SendFile(RESULT_FILE_LIST) < 0)
                break;

            FILE* fp_list = fopen(RESULT_FILE_LIST, "r");
            if (fp_list == NULL) {
                LOG("file open error");
                break;
            }

            char buf[10240];
            char filename[10240];

            while (fgets(buf, sizeof(buf), fp_list) != NULL) {
                int i = 0;
                while (true) {
                    if (buf[i] == '\n' || buf[i] == 0x00) {
                        buf[i] = 0x00;
                        break;
                    }
                    i++;
                }

                sprintf(filename, "result/%s", buf);

                if (mComm->SendInt(KEY) < 0)
                break;
                file_size = GetFileSize(filename);
                if (file_size < 0)
                break;
                if (mComm->SendInt(file_size) < 0)
                break;
                if (mComm->SendFile(filename) < 0)
                break;
            }

            fclose(fp_list);
        } else {
            if (command == 0) { //keep alive check
                        //do nothing
                    } else {
                        break;
                    }
                }

            }

            return 0;
        }

