//
// Copyright 2002 by HM Research Ltd. All rights reserved.
//
// Parser.h: interface for the CParser class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_PARSER_H__F2F49BBB_FC6F_4485_8E67_8ACC1326691C__INCLUDED_)
#define AFX_PARSER_H__F2F49BBB_FC6F_4485_8E67_8ACC1326691C__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <stdio.h>
#include <string>

using std::string;

class CParser {
  public:
  	CParser();
  	~CParser();
    int GetValue(const string field, const string key, int max_len, std::string* ret);
	int GetHeader(const string filename, char* problem, char* lang);
	
  private:
	int GetHeaderInfo(const string filename, const string key, char* value);
	int SearchField(FILE* fp, const string field);
};

#endif // !defined(AFX_PARSER_H__F2F49BBB_FC6F_4485_8E67_8ACC1326691C__INCLUDED_)
