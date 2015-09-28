//
// Copyright �� 2002 by HM Research Ltd. All rights reserved.
//
// Comm.h: interface for the CComm class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_)
#define AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_

#include <stdio.h>
#include <string>
#include "grade.pb.h"

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
	int RecvJob();
	int Close();
	CComm();
	virtual ~CComm();

	/*
	 * Releases ownership of the Grade protocolbuffer and returns it.
	 */
	model::Grade* releaseGradeProto();

	/*
	 * Sends the KEY, then the file length and then the file contents over
	 * the network.
	 *
	 * Returns whether everything proceeded correctly.
	 */
	bool SendDelimitedFile(const string filename);

	/*
	 * Sends a GradeResult protocol buffer over the network.
	 *
	 * Returns whether the sending was successful.
	 */
	bool SendProto(const model::GradeResult& proto);

private:
	int mSock;

	/*
	 * Reads up to size bytes from the network socket to vptr. Returns the
	 * number of bytes read. If there was a network error returns -1.
	 */
	int readn(char* vptr, int size);

	int readline(char* vptr, int maxlen);
	int writen(const char* vptr, int size);
	int RecvKey();
	int RecvInt();

	/*
	 * Parses a Grade protocol buffer from the network.
	 *
	 * Returns whether the parsing was successful.
	 */
	bool ParseGradeProto();

	std::auto_ptr<model::Grade> mGradeProto;
};

#endif // !defined(AFX_COMM_H__5E7A1DC5_4EFA_4E00_8182_D8F252C96153__INCLUDED_)
