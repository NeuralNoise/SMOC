//
// Copyright  2002 by HM Research Ltd. All rights reserved.
//
// Grader.h: interface for the CGrader class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GRADER_H__DF76EA8F_969C_4B7C_9B1B_5DB8A775A1C5__INCLUDED_)
#define AFX_GRADER_H__DF76EA8F_969C_4B7C_9B1B_5DB8A775A1C5__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "Config.h"
#include "Comm.h"
#include "Grade.h"
#include "Parser.h"

class CGrader {
  public:
	CGrader();
	~CGrader();
	int Init();
	int LoadConfig();
	int Process();

  private:
	CComm* mComm;
	CConfig* mConfig;
	CGrade* mGrade;
	CParser* mParser;

  private:
	void InitDirectories();
};

#endif // !defined(AFX_GRADER_H__DF76EA8F_969C_4B7C_9B1B_5DB8A775A1C5__INCLUDED_)
