//
// Copyright  2002 by HM Research Ltd. All rights reserved.
//
// Parser.cpp: implementation of the CParser class.
//
//////////////////////////////////////////////////////////////////////


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>
#include <string>

#include "common.h"
#include "Constants.h"
#include "Parser.h"

using namespace std;

CParser::CParser() {
}

CParser::~CParser() {
}

/*----------------
 // return value
 // n >= 0 : OK. (n : length of value string)
 // -1 : file open error
 // -2 : fail to find "key"
 //
 //----------------*/

//TODO : for Pascal
int CParser::GetHeaderInfo(const string filename, const string key, char* value) {
    FILE* fp = fopen(filename.c_str(), "r");

    if (fp == NULL) {
        LOG("ERROR : file open error (...in get_header_info func.) (%s)", strerror(errno));
        return -1;
    }

    char buf[10240];
    bzero(buf, sizeof(buf));
    //...
    int line = 0;
    unsigned int i, j;
    while (fgets(buf, sizeof(buf), fp) != NULL) {
        line++;
        if (line == 1 && strncasecmp(buf, "#FILE ", 6) == 0) {
            i = 6;
            j = 0;
            while (buf[i] == ' ' || buf[i] == '\t') {
                i++;
                if (i >= sizeof(buf)-1) {
                    value[j] = 0x00;
                    break;
                }
            }

            while (!isspace(buf[i])) {
                value[j] = buf[i];
                j++;
                i++;
                if (i >= sizeof(buf)-1 || j >= sizeof(buf)-1) {
                    value[j] = 0x00;
                    break;
                }
            }
            while (buf[i] == ' ' || buf[i] == '\t') {
                i++;
                if (i >= sizeof(buf)-1) {
                    value[j] = 0x00;
                    break;
                }
            }

            int num = atoi(buf+i);

            if (num > 0 && num <= 99) {
                sprintf(value+j, "%02d", num);
                fclose(fp);
                return j+2;
            }
        }

        if (strncasecmp(buf, key.c_str(), key.length()) == 0) {
            fclose(fp);
            i = key.length();
            j = 0;

            while (buf[i] == ' ' || buf[i] == ':') {
                if (i >= sizeof(buf)-1) {
                    value[j] = 0x00;
                    break;
                }
                i++;
            }

            while (buf[i] != ' ' && buf[i] != '\n' && buf[i] != '\r') {
                value[j] = buf[i];
                j++;
                i++;
                if (i >= sizeof(buf)-1 || j >= sizeof(buf)-1) {
                    value[j] = 0x00;
                    break;
                }
            }
            return j;
        }

        if (strncmp(buf, "*/", 2) == 0 || strncmp(buf, "}", 1) == 0) {
            fclose(fp);
            return -2;
        }
    }

    fclose(fp);
    return -2;
}

int CParser::GetValue(const string field, const string key, int max_len, string* ret) {
    FILE* fp = fopen(Constants::CONFIG_FILE.c_str(), "r");

    if (fp == NULL) {
        LOG("ERROR : file open error [filename:%s] (%s)", Constants::CONFIG_FILE.c_str(), strerror(errno));
        return -1;
    }

    int res;

    res = SearchField(fp, field);

    if (res < 0)
        return res;

    char buf[10240];

    while (fgets(buf, sizeof(buf), fp) != NULL) {
        if (buf[0] == '[') {
            fclose(fp);
            return -2;
        }

        int i = 0;
        int j = 0;

        while (buf[i] == ' ')
        i++;
        while (key[j] == ' ')
        j++;

        while (true) {
            if (key[j] == 0x00) {
                while (buf[i] == ' ')
                i++;
                if (buf[i] == '=') {
                    i++;
                    while (buf[i] == ' ')
                    i++;

                    *ret = "";
                    while (buf[i] != '\r' && buf[i] != '\n') {
                        *ret += buf[i];
                        i++;
                    }

                    fclose(fp);
                    return j;
                } else
                break;
            }

            if (strncasecmp(&buf[i], &key[j], 1) == 0) {
                i++;
                j++;
            } else
            break;
        }
    }

    fclose(fp);

    return -2;
}

/*----------------
 // return value
 // n = 0 : OK
 // -1 : invalid file pointer
 // -2 : fail to find field
 //
 //----------------*/
int CParser::SearchField(FILE* fp, const string field) {
    if (fp == NULL || field.empty())
        return -1;

    char buf[10240];

    while (fgets(buf, sizeof(buf), fp) != NULL) {
        if (buf[0] == '[') {
            int i = 1;
            int j = 0;

            while (buf[i] == ' ')
            i++;
            while (field[j] == ' ')
            j++;

            while (true) {
                if (field[j] == 0x00) {
                    while (buf[i] == ' ')
                    i++;
                    if (buf[i] == ']') {
                        return 0;
                    } else {
                        LOG("ERROR - No such field [%s]", field.c_str());
                        return -2;
                    }
                }

                if (strncasecmp(&buf[i], &field[j], 1) == 0) {
                    i++;
                    j++;
                } else
                break;
            }
        }
    }

    LOG("ERROR - No such field [%s]", field.c_str());
    return -2;
}

int CParser::GetHeader(const string filename, char* problem, char* lang) {
    int ret1, ret2;

    FILE* fp = fopen(filename.c_str(), "r");
    if (fp == NULL) {
        LOG("ERROR : file open error (...in get_header func.) (%s)", strerror(errno));
        return -1;
    }

    char buf[64];
    memset(buf, 0, 64);
    fscanf(fp, "%2s", buf);

    // This looks like a PE2 executable, return lang=EXE, find problem name from filename
//    if (strncmp(buf, "MZ", 2) == 0) {
//        int i;
//        lang = "EXE";
//
//        /*
//         fp = fopen(filename, "r");
//         if (fp == NULL) {
//         LOG("ERROR : file open error (...in get_header func.) (%s)", strerror(errno));
//         return -1;
//         }
//         */
//
//        if (fseek(fp, -24, SEEK_END) < 0) {
//            LOG("ERROR : file seek error (...in get_header func.) (%s)", strerror(errno));
//            return -1;
//        }
//        fscanf(fp, "%24s", buf);
//        if (strncmp(buf, "task", 4) != 0) {
//            return -2;
//        }
//        for (i = 4; isgraph(buf[i]); i++)
//            problem[i-4] = buf[i];
//        problem[i] = 0;
//
//        fclose(fp);
//        return 0;
//    }
    fclose(fp);

    ret1 = GetHeaderInfo(filename, "TASK", problem);
    ret2 = GetHeaderInfo(filename, "LANG", lang);

    if (ret1 == -1|| ret2 == -1) {
        return -1;
    } else if (ret1 == -2|| ret2 == -2) {
        return -2;
    }

    return 0;
}
