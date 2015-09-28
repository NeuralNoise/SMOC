/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 *
 * @author  Sunglim Lee
 * @version 1.00, 11/01/03
 */


import java.io.*;

public class LogSubmit extends Log
{
	protected static final boolean KOREAN_LANGUAGE_SUPPORT = false;
	protected static File dir = new File (".");
	protected static File file = new File (dir, "submit.log");
	private static PrintWriter out = null;
	
	public synchronized static void log(String in)
	{
	    out = _log(in, dir, file, KOREAN_LANGUAGE_SUPPORT, out);
	}
}
