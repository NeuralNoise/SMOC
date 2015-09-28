/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer.agent;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

import java.io.*;

public class LogGraderAgent extends kr.or.ioi2002.RMIServer.Log {
	private static final boolean KOREAN_LANGUAGE_SUPPORT = false;

	private static final File dir = new File(".");
	private static final File file = new File(dir, "agent_grader.log");
	private static PrintWriter out = null;

	public synchronized static void log(String in) {
		out = _log(in, dir, file, KOREAN_LANGUAGE_SUPPORT, out);
	}
}
