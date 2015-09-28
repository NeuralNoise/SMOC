/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 *
 * @author  Sunglim Lee
 * @version 1.00, 11/01/03
 */

//Source file: C:\\VisualCafeEE\\Projects\\kr\\or\\ioi2002\\RMIServer\\Util.java


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Util 
{
    // TODO: export somewhere else
    public static final java.text.DateFormat DATETIME_FORMAT = new java.text.SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    public static final java.text.DateFormat DATETIME_FORMAT_DATE = new java.text.SimpleDateFormat("yyyy.MM.dd");
    public static final java.text.DateFormat DATETIME_FORMAT_TIME = new java.text.SimpleDateFormat("HH:mm:ss");
	
	
	public static String stackTrace(Throwable ex)
	{
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
	}

	public static byte[] file2byte(File file) throws IOException 
	{
        if (file == null) return null;
        
        int length = (int)file.length();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length);

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        byte[] bBuffer = new byte[1024];
        int iTotal = 0;
        int iRead;
        while((iRead = bis.read(bBuffer)) > 0)
        {
            baos.write(bBuffer, 0, iRead);
            iTotal += iRead;
        }
        bis.close();
        
        return baos.toByteArray();		
	}
	
	static String getTime()
	{
        // get wall clock
        java.util.Date date = new java.util.Date();
        return DATETIME_FORMAT_TIME.format(date);
    }
    
    static boolean suffix(String str, String suf)
    {
        return (str.length() >= suf.length()) && (str.substring(str.length() - suf.length(), str.length()).equals(suf));
    }
}
