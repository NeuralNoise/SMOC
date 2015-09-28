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
    if (mParser.GetValue("CONFIG", "VERSION", 256, &version_) < 0) {
        LOG("can't find [CONFIG, VERSION]");
        return -1;
    }

    if (mParser.GetValue("SERVER", "ADDRESS", 256, &server_address_) < 0) {
        LOG("can't find [SERVER, ADDRESS]");
        return -1;
    }

    LOG("server [%s:%d]", server_address_.c_str(), Constants::SERVER_PORT);
    return 0;
}

const char* CConfig::GetVersion() {
    return version_.c_str();
}

string CConfig::server_address() {
    return server_address_;
}
