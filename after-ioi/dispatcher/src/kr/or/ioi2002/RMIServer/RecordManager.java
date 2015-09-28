/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

// Source file:
// C:\\VisualCafeEE\\Projects\\kr\\or\\ioi2002\\RMIServer\\RecordManager.java

import java.util.*;

public class RecordManager {
	private LinkedList<String> recordQ = new LinkedList<String>();

	/**
	 * @roseuid 3D3662D101B4
	 */
	public RecordManager() {

	}

	/**
	 * @param jobid
	 * @param data
	 * @roseuid 3D3662D101C8
	 */
	public void add(String record) {
		recordQ.add(record);
	}

	/**
	 * @return String
	 * @roseuid 3D3662D101F1
	 */
	public String dispatchNext() {
		if (recordQ.isEmpty())
			return null;
		return (String) recordQ.removeFirst();
	}
}
