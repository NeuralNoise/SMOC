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

abstract public class Log
{
	protected static PrintWriter _log(String in, File _dir, File _file, boolean bKoreanSupport, PrintWriter _out) 
	{   
	    try
        {   
            boolean isInit = false;
            if (_out == null || _out.checkError() || !_file.exists() || !_file.canWrite()) 
            {   
                _out = init(_dir, _file, bKoreanSupport, _out);
                isInit = true;
            }
            java.util.Date date = new java.util.Date();
            String strDate = Util.DATETIME_FORMAT.format(date);
            if (isInit) _out.println(strDate +",Logfile initialized");
            _out.println(strDate +","+ in); 
        } catch (IOException e)
        {   try
            {   
                _out = init(_dir, _file, bKoreanSupport, _out);
                java.util.Date date = new java.util.Date();
                String strDate = Util.DATETIME_FORMAT.format(date);
                    
                _out.println(strDate +",Logfile initialized");
                _out.println(strDate +","+ in); 
            }
            catch (Exception e2)
            {   System.out.println(e2);
            }
        }
        return _out;
	}
	
	/**
	@throws java.io.IOException
	@roseuid 3D364DF90216
	 */
	private static PrintWriter init(File _dir, File _file, boolean bKoreanSupport, PrintWriter _out) throws IOException 
	{   if (_out != null) _out.close();
        _dir.mkdirs();
            
        FileOutputStream fos = new FileOutputStream(_file.getCanonicalPath(), true);
        OutputStreamWriter osw = null;
        
        if (bKoreanSupport)
        {
            try
            {   osw = new OutputStreamWriter(fos, "EUC_KR");
            } catch (UnsupportedEncodingException e)
            {   System.out.println(e);
                osw = new OutputStreamWriter(fos);
                osw.write("Korean support disabled\n");
            }
        } else
        {
            osw = new OutputStreamWriter(fos);
        }
        
        _out = new PrintWriter(osw, true);		
        return _out;
	}
}
