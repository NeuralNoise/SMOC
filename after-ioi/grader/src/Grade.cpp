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
#include <iomanip>

#include "common.h"

using namespace std;

CGrade::CGrade() {
}

CGrade::~CGrade() {
}

int CGrade::compileExe(const string source_file, const string output_file,
		double* time) {
	struct stat file_stat;
	if (stat(output_file.c_str(), &file_stat) == 0) {
		int res = unlink(output_file.c_str());

		if (res == -1) {
			LOG("ERROR unlink (%s)", strerror(errno));
			return -3;
		}
	}
	stringstream ss;
	ss << "cp " << source_file << " " << output_file;
	system(ss.str().c_str());
	*time = 1;
	return 0;
}

bool CGrade::IsValidLanguage() {
	return mGradeProto->language() == LANG_PASCAL || mGradeProto->language()
			== LANG_C || mGradeProto->language() == LANG_CPP;
}

/*----------------
 // return value
 //	0 : OK
 // -1 : compile error
 // -2 : compile time over
 //----------------*/
int CGrade::Compile(const string& output_file, double* time) {
	if (!IsValidLanguage()) {
		LOG("No such language");
		return -4;
	}

	string link_file = "";
	if (mGradeProto->tasktype() == PROBLEM_TYPE_MODULE) {
		link_file = "lib/unbuffered_stdout.o";
	}

	if (mGradeProto->language() == LANG_EXE) {
		return compileExe(mPathname, output_file, time);
	}

	struct stat file_stat;
	if (mGradeProto->language() == LANG_JAVA) {
		string command = mPathname + ".java";
		if (stat(command.c_str(), &file_stat) == 0) {
			int res = unlink(command.c_str());
			if (res == -1) {
				LOG("ERROR unlink (%s)", strerror(errno));
				return -3;
			}
		}
		command = "cp " + mPathname + " " + mPathname + ".java";
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
	if (mGradeProto->language() == LANG_JAVA) {
		string command = "touch " + output_file;
		system(command.c_str());
		system("rm -f box/*.class");
	}

	if ((pid = fork()) < 0) {
		printf("fork error\n");
	} else if (pid == 0) {
		int output =
				open(COMPILE_RESULT, O_WRONLY | O_CREAT | O_TRUNC, S_IRWXU);

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

		if (mGradeProto->language() != LANG_PASCAL) {
			if (dup2(output, STDERR_FILENO) != STDERR_FILENO) {
				LOG("dup2 error to stderr (%s)", strerror(errno));
				exit(1);
			}
		} else {
			if (dup2(output, STDOUT_FILENO) != STDOUT_FILENO) {
				LOG("dup2 error to stderr (%s)", strerror(errno));
				exit(1);
			}
		}

		stringstream ss;
		if (mGradeProto->language() == LANG_PASCAL) {
			ss << "-o" << output_file.c_str();
			if (link_file.empty()) {
				if (execlp("fpc", "fpc", "-O2", "-XS", "-Sg", "-FEbox",
						ss.str().c_str(), mPathname.c_str(), NULL) < 0) {
					LOG("execlp ERROR(compile) (%s)", strerror(errno));
					exit(1);
				}
			} else {
				char linkage[100];
				sprintf(linkage, "-Fa%.*s", link_file.size() - 2,
						link_file.c_str() + 4); // HACK - delete .o from file

				if (execlp("fpc", "fpc", "-O2", "-XS", "-Sg", "-Fulib/pas",
						linkage, "-FEbox", ss.str().c_str(), mPathname.c_str(),
						NULL) < 0) {
					LOG("execlp ERROR(compile) (%s)", strerror(errno));
					exit(1);
				}
			}
		} else if (mGradeProto->language() == LANG_C) {
			if (link_file.empty()) {
				if (execlp("gcc", "gcc", "-o", output_file.c_str(),
						"-std=gnu99", "-O2", "-s", "-static", "-lm", "-x", "c",
						mPathname.c_str(), NULL) < 0) {
					LOG("execlp ERROR(compile) (%s)", strerror(errno));
					exit(1);
				}
			} else {
				if (execlp("gcc", "gcc", "-o", output_file.c_str(),
						"-std=gnu99", "-O2", "-s", "-static", "-lm",
						link_file.c_str(), "-x", "c", mPathname.c_str(),
						"-Ilib/c", NULL) < 0) {
					LOG("execlp ERROR(compile) (%s)", strerror(errno));
					exit(1);
				}
			}
		} else if (mGradeProto->language() == LANG_CPP) {
			if (link_file.empty()) {
				if (execlp("g++", "g++", "-o", output_file.c_str(), "-O2",
						"-s", "-static", "-lm", "-x", "c++", mPathname.c_str(),
						NULL) < 0) {
					LOG("execlp ERROR(compile) (%s)", strerror(errno));
					exit(1);
				}
			} else {
				if (execlp("g++", "g++", "-o", output_file.c_str(), "-O2",
						"-s", "-static", "-lm", link_file.c_str(), "-x", "c++",
						mPathname.c_str(), "-Ilib/c", NULL) < 0) {
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

	if (wait4(pid, &status, 0, &rusage) < 0) {
		LOG("wait error");
	}

	int sig = 0;
	if (WIFSIGNALED(status))
		sig = WTERMSIG(status);

	int exit_status = WEXITSTATUS(status);

	LOG("signal : %d, exit : %d (in compile)", sig, exit_status);

	if (mGradeProto->language() == LANG_JAVA && exit_status != 0) {
		unlink(output_file.c_str());
	}

	msec = (rusage.ru_utime.tv_sec + rusage.ru_stime.tv_sec) * 1000
			+ (rusage.ru_utime.tv_usec + rusage.ru_stime.tv_usec) / 1000;

	if (msec > 30* 1000 ) {
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

int CGrade::AppendFile(stringstream& output, const string read_file) {
	FILE* fp = fopen(read_file.c_str(), "r");
	char buf[10240];

	if (fp == NULL) {
		LOG("file open error! (%s)\n", read_file.c_str());
		return -1;
	}

	int max_appended_bytes = 100*1024;

	int printed = 0;
	while (printed < max_appended_bytes && fgets(buf, sizeof(buf), fp) != NULL) {
		printed += strlen(buf);
		output << buf;
	}
	if (printed > max_appended_bytes) {
		output << "\n<<< More data follows but is cut because it is too large. >>>\n";
	}
	fclose(fp);

	return 0;
}

int CGrade::AppendCompileResult(std::stringstream& sampleOutput,
		const string& compile_result) {
	FILE* fp = fopen(compile_result.c_str(), "r");
	char buf[10240];

	if (fp == NULL) {
		LOG("file open error! (%s)\n", compile_result.c_str());
		return -1;
	}

	while (fgets(buf, sizeof(buf), fp) != NULL) {
		if (strncasecmp(buf, mPathname.c_str(), mPathname.length()) == 0) {
			sampleOutput << string(buf + mPathname.length());
		} else if (strncasecmp(buf, mFilename.c_str(), mFilename.length()) == 0) {
			sampleOutput << string(buf + mFilename.length());
		} else {
			sampleOutput << string(buf);
		}
	}
	fclose(fp);

	return 0;
}

int CGrade::ProcessTest(const string& input_file) {
	stringstream testOuput;

	LOG("[TEST]type:%d", mGradeProto->tasktype());
	if (mGradeProto->tasktype() == PROBLEM_TYPE_OUTPUT) {
		mReturnResult = "RESULT TEST FAIL na\n";
		testOuput << " Output-only task needs no testing" << endl;
		mGradeResultProto->set_sampleoutput(testOuput.str());

		return 0;
	}

	testOuput << "[HEADER CHECK - OK]" << endl << "  task : "
			<< mGradeProto->taskname() << endl << "  language : "
			<< mGradeProto->language() << endl;

	//compile
	double compile_time, execute_time;

	int res = Compile(USER_PROGRAM, &compile_time);

	if (res < 0) {
		mReturnResult = "RESULT TEST FAIL " + mGradeProto->taskname() + "\n";

		testOuput << "[COMPILE - ERROR]" << endl;
		if (res == -1) {
			testOuput << "--[compiler message]------------" << endl;
			AppendCompileResult(testOuput, COMPILE_RESULT);
		} else if (res == -2) {
			testOuput << "  compile time limit exceeded!" << endl;
		}

		mGradeResultProto->set_accept(false);
		mGradeResultProto->set_sampleoutput(testOuput.str());

		return 0;
	}
	testOuput << "[COMPILE - OK]" << endl << "  compile time : " << setw(5)
			<< setprecision(2) << compile_time / 1000 << " seconds" << endl;
	//execute

	if (mGradeProto->tasktype() == PROBLEM_TYPE_STANDARD) {
		res = Execute(TEST_DATA, EXECUTE_RESULT, EXECUTE_STDERR, 0,
				&execute_time, NULL, NULL);
	} else { // (type == PROBLEM_TYPE_MODULE)
		res = Execute(TEST_DATA, EXECUTE_RESULT, EXECUTE_STDERR, 1,
				&execute_time, NULL, NULL);
	}

	if (res < 0) {
		mReturnResult = "RESULT TEST FAIL " + mGradeProto->taskname() + "\n";
		testOuput << "[EXECUTION - ERROR]" << endl;
		if (res == -1) {
			testOuput << "  execution time limit exceeded!" << endl;
		} else if (res == -2) {
			testOuput << "  execution error!" << endl;
		} else if (res == -3) {
			testOuput << "  execution error! (invalid memory reference)"
					<< endl;
		} else if (res == -4) {
			testOuput << "  output size limit exceeded!" << endl;
		} else if (res == -5) {
			testOuput << "  exit code is non-zero" << endl;
		} else if (res == -6) {
			testOuput << "  system error" << endl;
		} else
			LOG("Unknown return value");

		mGradeResultProto->set_sampleoutput(testOuput.str());
		return 0;
	}

	//test success!
	mReturnResult = "RESULT TEST OK " + mGradeProto->taskname() + "\n";

	testOuput << "[EXECUTION - OK]" << endl << "  execution time : " << setw(5)
			<< setprecision(2) << execute_time / 1000 << " seconds" << endl;
	testOuput << endl << "--[your output]--------------" << endl;
	AppendFile(testOuput, EXECUTE_RESULT);
	testOuput << endl;

	mGradeResultProto->set_sampleoutput(testOuput.str());

	return 0;
}

int CGrade::ProcessGradeForBatchOrInteractive() {
	stringstream sampleOutput;
	sampleOutput << "[HEADER CHECK - OK]" << endl << " Task:"
			<< mGradeProto->taskname() << " Language:"
			<< mGradeProto->language() << endl;

	//compile
	double compile_time, execute_time;

	for (int it = 0; it < mGradeProto->testindexes_size(); ++it) {
		mGradeResultProto->add_testindexes(mGradeProto->testindexes(it));
		mGradeResultProto->add_result("s");
	}
	char command[1024];

	int res = Compile(USER_PROGRAM, &compile_time);

	if (res == 0 || res == -1) {
		sprintf(command, "cp %s result/%s.compiler.out", COMPILE_RESULT,
				mGradeProto->taskname().c_str());
		system(command);
		fprintf(mListFile, "%s.compiler.out\n", mGradeProto->taskname().c_str());
	}

	if (res < 0) {
		mReturnResult = "RESULT GRADE FAIL " + mGradeProto->taskname() + "\n";

		for (int i = 0; i < mGradeProto->testindexes_size(); ++i) {
			mGradeResultProto->set_result(i, "c");
		}
		fprintf(mLogFile, "compile error\n");
		sampleOutput << "[COMPILE - ERROR]" << endl;

		if (res == -1) {
			sampleOutput << "--[compiler message]------------" << endl;
			AppendCompileResult(sampleOutput, COMPILE_RESULT);
		} else if (res == -2) {
			sampleOutput << "compile time limit exceeded!" << endl;
			fprintf(mLogFile, "compile time limit exceeded!\n");
		}

		mGradeResultProto->set_sampleoutput(sampleOutput.str());
		mGradeResultProto->set_accept(false);

		return 0;
	}

	mGradeResultProto->set_accept(true);
	sampleOutput << "[COMPILE - OK]" << endl << " compile time : " << setw(5)
			<< setprecision(2) << compile_time / 1000 << " seconds" << endl;

	mReturnResult = "RESULT GRADE OK " + mGradeProto->taskname() + "\n";
	fprintf(mLogFile,
			"task : %s\nlanguage : %s\ncompile time : %d\ndata_num : %d\n",
			mGradeProto->taskname().c_str(), mGradeProto->language().c_str(),
			(int) compile_time, mGradeProto->testscount());

	int exit_signal;
	int exit_code;

	sprintf(command, "cp %s result/%s.bin", USER_PROGRAM,
			mGradeProto->taskname().c_str());
	system(command);
	fprintf(mListFile, "%s.bin\n", mGradeProto->taskname().c_str());

	for (int it = 0; it < mGradeProto->testindexes_size(); ++it) {
		int index = mGradeProto->testindexes(it);

		char input_file[256], output_file[256], answer_file[256];
		sprintf(input_file, "data/%s.%.*d.in", mGradeProto->taskname().c_str(),
				TESTCASE_PADDING_LENGTH, index);
		sprintf(output_file, EXECUTE_RESULT);
		sprintf(answer_file, "data/%s.%.*d.sol",
				mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH, index);

		if (mGradeProto->tasktype() == PROBLEM_TYPE_STANDARD) {
			res = Execute(input_file, output_file, EXECUTE_STDERR, 0,
					&execute_time, &exit_signal, &exit_code);
		} else { // interactive task
			res = Execute(input_file, output_file, EXECUTE_STDERR, (index) ? 2
					: 1, &execute_time, &exit_signal, &exit_code);
		}

		fprintf(mLogFile, "input : %s, output :%s, sol : %s\n", input_file,
				output_file, answer_file);
		fprintf(mLogFile, "signal : %d, exit_code : %d\n", exit_signal,
				exit_code);
		fprintf(mLogFile, "Execute return value : %d\n", res);

		if (res < 0) {
			if (!index) {
				sampleOutput << "[SAMPLE DATA TEST - ERROR !]" << endl;
			}
			if (res == -1) {
				mGradeResultProto->set_result(it, "t");
				if (!index) {
					sampleOutput << "  execution time limit exceeded!" << endl;
				}
			} else if (res == -2) {
				mGradeResultProto->set_result(it, "e");
				if (!index) {
					sampleOutput << "  execution error!" << endl;
				}
			} else if (res == -3) {
				mGradeResultProto->set_result(it, "e");
				if (!index) {
					sampleOutput
							<< "  execution error! (invalid memory reference)"
							<< endl;
				}
			} else if (res == -4) {
				mGradeResultProto->set_result(it, "x");
				if (!index) {
					sampleOutput << "  output size limit exceeded!" << endl;
				}
			} else if (res == -5) {
				mGradeResultProto->set_result(it, "e");
				if (!index) {
					sampleOutput << "  exit code is non-zero" << endl;
					/*
					 * These lines are commented out as they are not valid languages
					 * at this point.
					 *
					 // append output of VM for Java and Win32 EXE
					 if (language == LANG_JAVA || language == LANG_EXE) {
					 AppendFile(fp_submit, output_file);
					 AppendFile(fp_submit, EXECUTE_STDERR);
					 }
					 */
				}
			} else {
				mGradeResultProto->set_result(it, "s");
				if (!index) {
					sampleOutput << "  system error" << endl;
				}
			}
			if (!index) {
				mGradeResultProto->set_accept(true);
				mGradeResultProto->set_sampleoutput(sampleOutput.str());
			}
		} else {
			fprintf(mLogFile, "execution time : %lf\n", execute_time);

			LOG("CHECK START");
			char checker[1024];
			sprintf(checker, "checker/%s.checker",
					mGradeProto->taskname().c_str());

			double points = CheckAnswer(input_file, output_file, answer_file,
					checker, mGradeProto->maxpoints(it));
			PrintPoints(it, points);

			if (!index) {
				if (points == 0) { //wrong answer
					sampleOutput << "[SAMPLE DATA TEST - ERROR !]" << endl
							<< "  execution time : " << setw(5)
							<< setprecision(2) << execute_time / 1000
							<< " seconds" << endl;

					struct stat file_stat;
					if (stat(answer_file, &file_stat) != 0) {
						LOG("ERROR : stat error (%s)", strerror(errno));
					}
					if (file_stat.st_size != 0) {
						sampleOutput
								<< "--- [SAMPLE DATA TEST - correct answer] ---"
								<< endl;
						AppendFile(sampleOutput, answer_file);
						sampleOutput << endl;
					}

					sampleOutput << "--- [SAMPLE DATA TEST - your answer] ---"
							<< endl;
					AppendFile(sampleOutput, output_file);
					sampleOutput << endl;

				} else if (points < 0) {
					sampleOutput << "system error !!!" << endl;
				} else {
					sampleOutput << "[SAMPLE DATA TEST - OK]" << endl
							<< "  execution time : " << setw(5)
							<< setprecision(2) << execute_time / 1000
							<< " seconds" << endl;
				}

				if (points >= 0) {
					FILE* fp_checker = fopen(CHECK_RESULT, "r");
					if (fp_checker != NULL) {
						sampleOutput
								<< "---[SAMPLE DATA TEST - checker message] ---"
								<< endl;
						char temp_buf[10240];
						fgets(temp_buf, sizeof(temp_buf), fp_checker);
						while (fgets(temp_buf, sizeof(temp_buf), fp_checker)
								!= NULL) {
							sampleOutput << temp_buf;
						}
						fclose(fp_checker);
						sampleOutput << endl;
					}
				}
				mGradeResultProto->set_accept(true);
				mGradeResultProto->set_sampleoutput(sampleOutput.str());
			}

			sprintf(command, "cp %s result/%s.%.*d.out", EXECUTE_RESULT,
					mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH,
					index);
			system(command);
			fprintf(mListFile, "%s.%.*d.out\n",
					mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH,
					index);

			sprintf(command, "cp %s result/%s.%.*d.checker.out", CHECK_RESULT,
					mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH,
					index);
			system(command);
			fprintf(mListFile, "%s.%.*d.checker.out\n",
					mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH,
					index);
		}
	}
	mGradeResultProto->set_sampleoutput(sampleOutput.str());
	return res;
}

void CGrade::ProcessGradeForOutputOnly() {
	//checker
	const int x_index = mGradeProto->testindexes(0);

	//success
	stringstream ss;
	ss << "RESULT GRADE OK " << mGradeProto->taskname() << setw(
			TESTCASE_PADDING_LENGTH) << setfill('0') << x_index << endl;
	mReturnResult = ss.str();

	//////////////////////////////////////////////

	stringstream output;
	output << "[HEADER CHECK - OK]" << endl;
	output << "  task : " << mGradeProto->taskname() << endl;

	//format check
	char format[1024];
	sprintf(format, "checker/%s.format", mGradeProto->taskname().c_str());

	char input_file[256];
	char sol_file[256];
	sprintf(input_file, "data/%s.%.*d.in", mGradeProto->taskname().c_str(),
			TESTCASE_PADDING_LENGTH, x_index);
	sprintf(sol_file, "data/%s.%.*d.sol", mGradeProto->taskname().c_str(),
			TESTCASE_PADDING_LENGTH, x_index);

	double points = CheckAnswer(input_file, mPathname, "no_meaning", format,
			mGradeProto->maxpoints(0));

	if (points <= 0) { //format error
		output << "[FORMAT CHECK - ERROR]" << endl;
	} else {
		output << "[FORMAT CHECK - OK]" << endl;
	}

	FILE* fp_checker = fopen(CHECK_RESULT, "r");
	if (fp_checker != NULL) {
		output << endl << "--[checker message]----------" << endl;
		char temp_buf[10240];
		fgets(temp_buf, sizeof(temp_buf), fp_checker);
		while (fgets(temp_buf, sizeof(temp_buf), fp_checker) != NULL) {
			output << temp_buf;
		}
		fclose(fp_checker);
		output << endl;
	}

	mGradeResultProto->set_accept(points > 0);
	mGradeResultProto->set_sampleoutput(output.str());
	mGradeResultProto->add_testindexes(x_index);
	mGradeResultProto->add_result("s");

	//////////////////////////////////////////////
	char checker[1024];
	sprintf(checker, "checker/%s.checker", mGradeProto->taskname().c_str());

	points = CheckAnswer(input_file, mPathname, sol_file, checker,
			mGradeProto->maxpoints(0));
	PrintPoints(0, points);

	char command[1024];
	sprintf(command, "cp %s result/%s.%.*d.checker.out", CHECK_RESULT,
			mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH, x_index);
	system(command);
	fprintf(mListFile, "%s.%.*d.checker.out\n",
			mGradeProto->taskname().c_str(), TESTCASE_PADDING_LENGTH, x_index);
}

int CGrade::ProcessGrade() {
	mLogFile = fopen(GRADE_LOG, "w+");
	mListFile = fopen(RESULT_FILE_LIST, "w+");

	if (mLogFile == NULL) {
		LOG("file open error [%s]", GRADE_LOG);
		return -1;
	}
	if (mListFile == NULL) {
		LOG("file open error [%s]", RESULT_FILE_LIST);
		return -1;
	}

	LOG("Header info: %s %s\n", mGradeProto->taskname().c_str(),
			mGradeProto->language().c_str());

	int res;
	int type = mGradeProto->tasktype();
	if (type == PROBLEM_TYPE_STANDARD || type == PROBLEM_TYPE_MODULE) {
		res = ProcessGradeForBatchOrInteractive();
	} else if (type == PROBLEM_TYPE_OUTPUT) {
		ProcessGradeForOutputOnly();
	}

	fclose(mLogFile);
	fclose(mListFile);
	return 0;
}

double CGrade::CheckAnswer(const string& input_file, const string& output_file,
		const string& answer_file, const string& checker,
		const string& max_points) {

	LOG("[CHECK ANSWER]input:%s, output:%s, answer:%s max_points:%s",
			input_file.c_str(), output_file.c_str(), answer_file.c_str(),
			max_points.c_str());

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

		if (execl(checker.c_str(), checker.c_str(), input_file.c_str(),
				output_file.c_str(), answer_file.c_str(), max_points.c_str(),
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

	if (fgets(buf, sizeof(buf), fp) == NULL) {
		return -2;
	}

	fclose(fp);

	double points;
	sscanf(buf, "%lf", &points);

	fp = fopen(CHECK_RESULT, "r");

	while (fgets(buf, sizeof(buf), fp) != NULL) {
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
 //----------------*/
int CGrade::Execute(const string& input_file, const string& output_file,
		const string& error_file, int flag, // 0: no module (standard) | 1: test module | 2: grade module
		double* time, int* exit_signal, int* exit_code) {
	if (!IsValidLanguage()) {
		LOG("Wrong executable type");
		return -7;
	}

	LOG("[Execute] input:%s, output:%s, limit:%d:%d:%d", input_file.c_str(),
			output_file.c_str(), mGradeProto->timelimit(),
			mGradeProto->memorylimit(), mGradeProto->outputlimit());

	char time_limit_s[20];
	sprintf(time_limit_s, "%lf", mGradeProto->timelimit() / 1000.0);
	char mem_limit_s[20];
	sprintf(mem_limit_s, "%d", mGradeProto->memorylimit() * 1024);
	char wall_limit_s[20];
	sprintf(wall_limit_s, "%lf",
			max(2* mGradeProto ->timelimit() / 1000.0, 2.5));
	char output_limit_s[20];
	sprintf(output_limit_s, "%d", mGradeProto->outputlimit());

	int pipes[2][2];
	if (flag) {
		if (pipe(pipes[0]) == -1 || pipe(pipes[1]) == -1) {
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
			for (int i = 3; i < 256; i++)
				close(i); // else might inherit some fd from grader

			if (execl("./sandbox", "./sandbox", "-a", "2", "-f", "-T", "-i",
					input_file.c_str(), "-o", output_file.c_str(), "-r",
					error_file.c_str(), "-O", output_limit_s, "-m",
					mem_limit_s, "-t", time_limit_s, "-w", wall_limit_s,
					"./box/tmp.exe", NULL) < 0) {
				LOG("execl ERROR(run) (%s)", strerror(errno));
				exit(11);
			}
		} else {
			if (dup2(pipes[0][0], 0) != 0 || dup2(pipes[1][1], 1) != 1) {
				LOG("Error redirecting pipes (%s)", strerror(errno));
				exit(11);
			}

			for (int i = 3; i < 256; i++)
				close(i); // else might inherit some fd from grader

			if (execl("./sandbox", "./sandbox", "-a", "2",
					"-f",
					"-T", // -i, -o inherited
					"-r", error_file.c_str(), "-O", output_limit_s, "-m",
					mem_limit_s, "-t", time_limit_s, "-w", wall_limit_s,
					"./box/tmp.exe", NULL) < 0) {
				LOG("execl ERROR(run) (%s)", strerror(errno));
				exit(11);
			}
		}
	} else {
		int status;

		if (flag) {
			char module[1024];
			sprintf(module, "checker/%s.module%s",
					mGradeProto->taskname().c_str(), (flag == 1) ? "_test" : "");

			pid_t module_pid;
			if ((module_pid = fork()) < 0) {
				LOG("fork error");
				return -6;
			} else if (module_pid == 0) {
				close(2);
				if (open("./tmp/pipe.module.stderr", O_WRONLY | O_CREAT
						| O_TRUNC, 0666) != 2) {
					LOG(
							"Unable to open ./tmp/pipe.module.stderr for writing (%s)",
							strerror(errno));
					exit(11);
				}

				if (dup2(pipes[1][0], 0) != 0 || dup2(pipes[0][1], 1) != 1) {
					LOG("Error redirecting pipes (%s)", strerror(errno));
					exit(11);
				}
				for (int i = 0; i < 4; i++)
					close(((int *) pipes)[i]);

				if (execl("./sandbox", "./sandbox", "-w", "60", module, // -i -o inherited
						input_file.c_str(), output_file.c_str(), NULL) < 0) {
					LOG("execl ERROR(run) (%s)", strerror(errno));
					exit(11);
				}
			}

			for (int i = 0; i < 4; i++)
				close(((int *) pipes)[i]);

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
		char buff[1024];
		memset(buff, 0, sizeof(buff));
		read(box_result, buff, 1024);
		close(box_result);
		LOG("BOX: %s\n", buff);

		int v1;
		double v2, v3;
		if (sscanf(buff,
				"OK (%lf sec real, %lf sec wall, %lf MB, %d syscalls)", time,
				&v3, &v2, &v1) == 4) {
			*time = *time * 1000;
			if (exit_signal != NULL)
				*exit_signal = 0;
			if (exit_code != NULL)
				*exit_code = 0;
			return 0;
		}
		if (strncmp(buff, "Time limit exceeded", 19) == 0) {
			if (exit_signal != NULL)
				*exit_signal = 0;
			if (exit_code != NULL)
				*exit_code = 0;
			return -1;
		}
		if ((sscanf(buff, "Received signal %d", &v1) == 1) || (sscanf(buff,
				"Committed suicide by signal %d", &v1) == 1) || (sscanf(buff,
				"Caught fatal signal %d", &v1) == 1) || ((strncmp(buff,
				"Interrupted", 11) == 0) && (v1 = SIGINT)) || // it is v1= not ==
				((strncmp(buff, "Breakpoint", 10) == 0) && (v1 = SIGINT)) // it is v1= not ==
		) {
			if (exit_signal != NULL)
				*exit_signal = v1;
			if (exit_code != NULL)
				*exit_code = 1;

			if (v1 == SIGXFSZ)
				return -4;
			else
				return -2;
		}
		if (sscanf(buff, "Exited with error status %d", &v1) == 1) {
			if (exit_signal != NULL)
				*exit_signal = 0;
			if (exit_code != NULL)
				*exit_code = v1;
			return -5;
		}
		if (strncmp(buff, "File", 4) != 0 || strncmp(buff, "Access", 6) != 0
				|| strncmp(buff, "open(", 5) != 0 || strncmp(buff,
				"lseek64(mem): ", 14) != 0 || strncmp(buff, "read(mem): ", 11)
				!= 0 || strncmp(buff, "Forbidden access to file ", 25) != 0
				|| strncmp(buff, "read on /proc/", 14) != 0 || strncmp(buff,
				"/proc/", 6) != 0 || strncmp(buff, "proc stat syntax error ",
				23) != 0 || strncmp(buff, "Unknown syscall", 15) != 0
				|| strncmp(buff, "Syscall ", 8) != 0 || strncmp(buff,
				"FO: Forbidden syscall ", 22) != 0) {
			if (exit_signal != NULL)
				*exit_signal = 0;
			if (exit_code != NULL)
				*exit_code = 0;
			return -2;
		}
	}
	return -6;
}

void CGrade::setGradeProto(model::Grade* proto) {
	mGradeProto.reset(proto);
}

void CGrade::setGradeResultProto(model::GradeResult* proto) {
	mGradeResultProto.reset(proto);
}

void CGrade::PrintPoints(int proto_index, double points) {
	fprintf(mLogFile, "points : %lf\n", points);
	if (points <= 0) {
		mGradeResultProto->set_result(proto_index, "x");
	} else {
		char points_str[128];
		sprintf(points_str, "%.9lg", points);
		mGradeResultProto->set_result(proto_index, points_str);
	}
}

void CGrade::setFilename(const string& path, const string& filename) {
	mPathname = path + filename;
	mFilename = filename;
}

const string& CGrade::getReturnResult() {
	return mReturnResult;
}

const model::GradeResult& CGrade::getGradeResultProto() {
	return *mGradeResultProto;
}
