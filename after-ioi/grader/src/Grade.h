//
// Copyright 2002 by HM Research Ltd. All rights reserved.
//
// Grade.h: interface for the CGrade class.
//
//////////////////////////////////////////////////////////////////////

#if !defined(AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_)
#define AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000
#include <sys/types.h>
#include <sstream>

#include "Parser.h"
#include "Config.h"
#include "grade.pb.h"

class CGrade {
public:
	CGrade();
	~CGrade();

	int Compile(const string& output_file, double* time);
	int Execute(const string& input_file, const string& output_file,
			const string& error_file, int flag, double* time, int* signal,
			int* exit_code);
	int ProcessTest(const string& input_file);
	int ProcessGrade();

	void setGradeProto(model::Grade* proto);
	void setGradeResultProto(model::GradeResult* proto);
	void setFilename(const string& path, const string& filename);
	const model::GradeResult& getGradeResultProto();

	const std::string& getReturnResult();

private:
	FILE* mListFile;
	FILE* mLogFile;

	std::auto_ptr<model::Grade> mGradeProto;
	std::auto_ptr<model::GradeResult> mGradeResultProto;
	std::string mPathname;
	std::string mFilename;
	std::string mReturnResult;

	int AppendCompileResult(std::stringstream& sampleOutput,
			const string& compile_result);

	int AppendFile(std::stringstream& output, const string read_file);

	double CheckAnswer(const string& input_file, const string& output_file,
			const string& answer_file, const string& checker, const string& max_points);
	int compileExe(const string source_file, const string output_file,
			double* time);

	bool IsValidLanguage();

	int ProcessGradeForBatchOrInteractive();
	void ProcessGradeForOutputOnly();
	void PrintPoints(int proto_index, double points);
};

#endif // !defined(AFX_GRADE_H__89B39C1D_2CD2_4BE1_AFFE_58ABB06AB803__INCLUDED_)
