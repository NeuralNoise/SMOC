//
// Copyright �� 2002 by HM Research Ltd. All rights reserved.
//
// Comm.cpp: implementation of the CComm class.
//
//////////////////////////////////////////////////////////////////////

#include "Comm.h"

#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <stdarg.h>

#include "common.h"
#include "grade.pb.h"

//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CComm::CComm() {
}

CComm::~CComm() {
}

int CComm::Connect(const char *addr, int port, const char* version) {
	struct sockaddr_in servaddr;

	mSock = socket(AF_INET, SOCK_STREAM, 0);

	if (mSock == -1) {
		LOG("socket ERROR (%s)", strerror(errno));
		return -1;
	}

	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(port);
	inet_pton(AF_INET, addr, &servaddr.sin_addr);

	if (::connect(mSock, (const struct sockaddr*) &servaddr, sizeof(servaddr))
			< 0) {
		LOG("connect ERROR (%s), %s:%d", strerror(errno), addr, port);
		close(mSock);
		return -1;
	}

	LOG(".....connected!  [VERSION:%s]", version);

	int res;
	char buf[1024];

	//0815 wook
	sprintf(buf, "READY %s\n", version);

	res = SendMsg(buf);

	//res = SendMsg("READY\n");
	//0815

	if (res < 0) {
		close(mSock);
		return -1;
	}

	bzero(buf, sizeof(buf));

	int nread = readline(buf, 1024);

	if (nread < 0) {
		close(mSock);
		return -1;
	} else if (nread == 0) //0815
	{
		LOG("Connection closed!");
		close(mSock);
		return -1;
	}

	LOG("[RECV:%s]", buf);

	if (strncasecmp(buf, "ACK\n", 4) != 0) {
		LOG("Protocol ERROR [ACK]");
		return -1;
	}

	return mSock;
}

int CComm::readn(char* vptr, int size) {
	int nleft = size;
	int nread = 0;

	char* ptr = vptr;

	while (nleft > 0) {
		if ((nread = read(mSock, ptr, nleft)) < 0) {
			if (errno == EINTR)
				nread = 0;
			else
				return -1;
		} else if (nread == 0) {
			break;
		}
		nleft -= nread;
		ptr += nread;
	}

	return size - nleft;
}

int CComm::readline(char* vptr, int maxlen) {
	int n, rc;
	char c, *ptr;

	ptr = vptr;

	for (n = 1; n < maxlen; n++) {
		if ((rc = read(mSock, &c, 1)) == 1) {
			*ptr++ = c;
			if (c == '\n')
				break;
		} else if (rc == 0) {
			if (n == 1)
				return 0;
			else
				break;
		} else {
			if (errno == EINTR) {
				n--;
				continue;
			}
			return -1;
		}
	}

	*ptr = 0;

	return n;
}

int CComm::RecvFile(int len, const string filename) {
	FILE* fp = fopen(filename.c_str(), "w+");
	if (fp == NULL) {
		LOG("file open ERROR (%s)", strerror(errno));
		return -1;
	}

	int nleft;
	int nread;
	char buf[1024];

	nleft = len;

	while (nleft > 0) {
		if ((nread = read(mSock, buf, (nleft > 1024) ? 1024 : nleft)) < 0) {
			if (errno == EINTR)
				nread = 0;
			else
				return -1;
		} else if (nread == 0)
			break;

		if ((fwrite(buf, sizeof(char), nread, fp)) != (unsigned) nread) {
			LOG("file write ERROR");
		}

		nleft -= nread;
	}

	fclose(fp);

	return len;
}

int CComm::SendInt(int value) {
	int buf;

	buf = htonl(value);

	if (writen((char*) &buf, sizeof(int)) != sizeof(int))
		return -1;

	LOG("[SEND INT:%d]", value);

	return 0;
}

int CComm::SendFile(const string filename) {
	FILE* fp = fopen(filename.c_str(), "r");
	if (fp == NULL) {
		LOG("file open ERROR [filename:%s] (%s)", filename.c_str(), strerror(
				errno));
		return -1;
	}

	char buf[1024];
	int len = 0;
	int nread;

	while (true) {
		bzero(buf, sizeof(buf));

		nread = fread(buf, sizeof(char), sizeof(buf), fp);

		if (nread == 0 && feof(fp))
			break;

		if (writen(buf, nread) != nread) {
			fclose(fp);
			return -1;
		}

		len += nread;
	}

	fclose(fp);

	LOG("[SEND:FILE:%s]", filename.c_str());

	return len;
}

int CComm::writen(const char* vptr, int size) {
	int nleft;
	int nwritten;

	const char* ptr = vptr;
	nleft = size;

	while (nleft > 0) {
		if ((nwritten = write(mSock, ptr, nleft)) <= 0) {
			if (errno == EINTR)
				nwritten = 0;
			else
				return -1;
		}
		nleft -= nwritten;
		ptr += nwritten;
	}

	return size;
}

int CComm::SendMsg(const string msg) {
	int size = msg.length();

	if (writen(msg.c_str(), size) != size) {
		LOG("SendMsg ERROR [msg:%s]", msg.c_str());
		return -1;
	}

	LOG("[SEND:%s]", msg.c_str());

	return 0;
}

bool CComm::ParseGradeProto() {
	int len = RecvInt();
	if (len <= 0)
		return false;

	model::Grade gradeProto;
	char buf[len + 4];

	int bytes_read = readn(buf, len);
	if (bytes_read != len) {
		LOG("Error reading Grade protocol buffer!");
		return false;
	}
	if (!gradeProto.ParseFromArray(buf, len)) {
		LOG("Could not parse the grade protobuf!");
		return false;
	}

	if (mGradeProto.get() == NULL) {
		mGradeProto.reset(new model::Grade());
	}
	mGradeProto->CopyFrom(gradeProto);

	return true;
}

/*----------------
 // return value
 // -1 : error
 // 1 : request submit // no longer returned!!
 // 2 : request test
 // 3 : request grade
 // 0 : keep alive CHECK
 //
 //----------------*/
int CComm::RecvJob() {
	char buf[1024];
	bzero(buf, sizeof(buf));

	int nread = readline(buf, 1024);

	if (nread < 0) {
		close(mSock);
		return -1;
	} else if (nread == 0) {
		LOG("Connection closed!");
		close(mSock);
		return -1;
	}

	int len;

	LOG("[RECV:%s]", buf);

	char source_path[256];

	sprintf(source_path, "%s%s", TEMP_PATH, SOURCE_FILE);

	if (strncasecmp(buf, "REQUEST TEST", strlen("REQUEST TEST")) == 0) {
		if (!ParseGradeProto()) {
			return -1;
		}

		if (RecvKey() < 0)
			return -1;
		len = RecvInt();
		if (len < 0)
			return -1;
		if (RecvFile(len, source_path) < 0)
			return -1;

		if (RecvKey() < 0)
			return -1;
		len = RecvInt();
		if (len < 0)
			return -1;
		if (RecvFile(len, TEST_DATA) < 0)
			return -1;

		return 2;
	} else if (strncasecmp(buf, "REQUEST GRADE", strlen("REQUEST GRADE")) == 0) {
		if (!ParseGradeProto()) {
			return -1;
		}

		if (RecvKey() < 0)
			return -1;
		len = RecvInt();
		if (len < 0)
			return -1;
		if (RecvFile(len, source_path) < 0)
			return -1;

		return 3;
	} else if (strncasecmp(buf, "\n", 1) == 0) {
		return 0;
	} else {
		LOG("Protocol Error [received:%s]", buf);
		close(mSock);
		return -1;
	}
}

/*----------------
 // return value
 // -1 : recv error
 // -2 : invalid key
 // 0 : O.K.
 //----------------*/
int CComm::RecvKey() {
	int key;

	key = RecvInt();
	if (key != KEY) {
		LOG("Protocol Error (invalid key)");
		return -2;
	}

	return 0;
}

int CComm::RecvInt() {
	int tmp;

	if (readn((char*) &tmp, 4) != 4)
		return -1;

	tmp = ntohl(tmp);

	return tmp;
}

int CComm::SendFile(const string filename, int max_size) {
	FILE* fp = fopen(filename.c_str(), "r");
	if (fp == NULL) {
		LOG("file open ERROR [filename:%s] (%s)", filename.c_str(), strerror(
				errno));
		return -1;
	}

	char buf[1024];
	int len = 0;
	int nread;

	while (true) {
		bzero(buf, sizeof(buf));

		nread = fread(buf, sizeof(char), sizeof(buf), fp);

		if (nread == 0 && feof(fp))
			break;

		if (len + nread >= max_size) {
			if (writen(buf, max_size - len) != max_size - len) {
				fclose(fp);
				return -1;
			}

			fclose(fp);
			LOG("[SEND:FILE:%s, truncated]", filename.c_str());
			return max_size;
		}

		if (writen(buf, nread) != nread) {
			fclose(fp);
			return -1;
		}

		len += nread;
	}

	fclose(fp);

	LOG("[SEND:FILE:%s]", filename.c_str());

	return len;
}

int CComm::Close() {
	close(mSock);
	return 0;
}

bool CComm::SendDelimitedFile(string filename) {
	int file_size = GetFileSize(filename);
	if (file_size < 0) {
		return false;
	}
	if (SendInt(KEY) < 0) {
		return false;
	}
	if (SendInt(file_size) < 0) {
		return false;
	}
	if (SendFile(filename) < 0) {
		return false;
	}
	return true;
}

model::Grade* CComm::releaseGradeProto() {
	return mGradeProto.release();
}

bool CComm::SendProto(const model::GradeResult& proto) {
	int size = proto.ByteSize();
	if (SendInt(size) < 0) {
		return false;
	}
	char message[size + 4];
	proto.SerializeToArray(message, size);
	int written = writen(message, size);
	return (written == size);
}
