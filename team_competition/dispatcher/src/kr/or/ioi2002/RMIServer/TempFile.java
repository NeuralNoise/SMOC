/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 *
 * @author  Sunglim Lee
 * @version 1.00, 11/01/03
 */

//Source file: C:\\VisualCafeEE\\Projects\\kr\\or\\ioi2002\\RMIServer\\TempFile.java


import java.io.*;

public class TempFile extends File 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6428558246829757480L;

	private static final File TEMPFILE_ROOT = new File("." + File.separator + "TEMP");

	private boolean autodelete = true;
	private boolean bDeleted = false;
	
	/**
	@param file
	@throws java.io.IOException
	@roseuid 3D364E520368
	 */
	private TempFile(File file) throws IOException 
	{
        super(file.getCanonicalPath());		
	}
	
	/**
	@roseuid 3D364E4A0032
	 */
	public static void delall() 
	{
        String[] aFiles = TEMPFILE_ROOT.list();
        for (int i = 0; i < aFiles.length; i++)
        {
            boolean bResult = (new File(aFiles[i])).delete();
            if (!bResult)
                Syslog.log("TempFile: cleanup: delete failed; "+aFiles[i]);
        }		
	}
	
	/**
	@param filename
	@param buffer
	@return TempFile
	@throws java.io.IOException
	@roseuid 3D364E4A0050
	 */
	public static TempFile createFromByteArray(byte[] buffer) throws IOException 
	{
        if (!TEMPFILE_ROOT.exists()) TEMPFILE_ROOT.mkdirs();
            
        TempFile tmp = new TempFile(File.createTempFile("cfb", null, TEMPFILE_ROOT));

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp));
        bos.write(buffer, 0, buffer.length);
        bos.close();
        
        return tmp;
	}
	
	/**
	@param filename
	@param is
	@param length
	@return TempFile
	@throws java.io.IOException
	@roseuid 3D364E4D00B8
	 */
	public static TempFile createFromStream(InputStream is, int length) throws IOException 
	{
        if (!TEMPFILE_ROOT.exists()) TEMPFILE_ROOT.mkdirs();
            
        TempFile tmp = new TempFile(File.createTempFile("cfs", null, TEMPFILE_ROOT));

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmp));
        
        byte[] buffer = new byte[1024];
        int iTotalRead = 0;
        while (iTotalRead < length)
        {
            int iNextRead = 1024;
            if (length - iTotalRead < 1024) iNextRead = length - iTotalRead;
            int iRead = 0;
            int iOffset = 0;
            while ((iRead = is.read(buffer, iOffset, iNextRead- iOffset)) > 0) iOffset += iRead;
            if (iRead == -1) throw new IOException("TempFile: createFromStream: end of file reached before length");
            bos.write(buffer, 0, iNextRead);
            iTotalRead += iNextRead;
        }
        
        bos.close();        
        return tmp;		
	}
	
/*
	public static TempFile moveFromPath(String filename, String path) throws IOException 
	{
        File dir = new File(TEMPFILE_ROOT);
        if (!dir.exists()) dir.mkdirs();

        TempFile tmp = new TempFile(File.createTempFile("mfp", null, new File(TEMPFILE_ROOT)));
        
        File src = new File(path);
        
        boolean bResult = tmp.delete();
        if (!bResult) throw new IOException("TempFile: moveFromPath: delete failed: file("+tmp.toString()+")");
        bResult = src.renameTo(tmp);
        if (!bResult) throw new IOException("TempFile: moveFromPath: renameTo failed: src("+src.toString()+") file("+tmp.toString()+")");
        
        return tmp;		
	}
*/	
	/**
	@param filename
	@return boolean
	@roseuid 3D364E570398
	 */
	public boolean makePermanent(File filepath) 
	{
	    if (bDeleted)
	    {
	        Syslog.log("!delTmp() was called before: makePermanent: ["+this.toString()+"] to ["+filepath+"]");
	        return false;
	    }
	    
        boolean ret = this.renameTo(filepath);
        if (!ret) 
        {
            Syslog.log("!rename failed: makePermanent: ["+this.toString()+"] to ["+filepath+"]");
        }
        
        autodelete = false;
        return ret;		
	}
	
	/**
	@roseuid 3D364E580078
	 */
	public void delTmp() 
	{
        if (this.exists())
        {
            boolean bResult = this.delete();
            if (!bResult) Syslog.log("Failed to delete at TempFile: "+this.toString());
        }		
        bDeleted = true;
	}
	
	/**
	Override
	@throws java.lang.Throwable
	@roseuid 3D364E5800D2
	 */
	protected void finalize() throws Throwable 
	{
        if (autodelete) delTmp();		
	}
}
