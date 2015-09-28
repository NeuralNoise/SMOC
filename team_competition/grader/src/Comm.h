//
// Copyright �� 2002 by HM Research Ltd. All rights reserved.
//
// Comm.h: interface for the CComm class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_)
#define AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <stdio.h>
#include <string>

using std::string;

#define KEY 0x37fd8a20

class CComm {
public:
	int Connect(const char* addr, int port, const char* version);
	int RecvFile(int len, const string filename);
	int SendFile(const string filename);
	int SendFile(const string filename, int max_size);
	int SendInt(int value);
	int SendMsg(const string msg);
	int ParseTaskData(const char * buf, string * task, string * language);
	int RecvJob(string * task, string * language);
	int Close();
	CComm();
	virtual ~CComm();

private:
	int mSock;
	int readn(char* vptr, int size);
	int readline(char* vptr, int maxlen);
	int writen(const char* vptr, int size);
	int RecvKey();
	int RecvInt();

};

#endif // !defined(AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_)
