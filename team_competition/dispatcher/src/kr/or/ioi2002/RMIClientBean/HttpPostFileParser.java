/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIClientBean;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

/*******************************************************************************
 * ID : EdsUploadRequest.java
 * 
 * Version : 1.00
 * 
 * �۾��� : �̼���
 * 
 * �۾��� : 2000�� 12�� 21��
 * 
 * ���� : ���� ��� ���
 * 
 * ��� TABLE:
 * 
 * �Է� : request
 * 
 * ��� :
 * 
 * ************************* �� �� �� ��
 * **************************************** ��ȣ �� �� �� �� �� �� ���泻�� -----
 * -------- ---------------- --------------------------------------- 1 �̼���
 * 2000�� 12�� 21�� ���� �ۼ� 2 �̼��� 2000�� 12�� 27�� �ѱ� �� �ѱ����ϸ� ��� �߰� 3
 * �̼��� 2001�� 01�� 16�� �� ���� ó�� �߰� 4 �̼��� 2001�� 02�� 20�� ��� ��d ��
 * �޸� �� ��2ȭ (�ӽ����� ���) 5 �̼��� 2002�� 06�� 24�� Ȯ���ڰ� ��� ���� ��ε� ��
 * ��� ��d 6 �̼��� 2002�� 07�� 25�� �ִ� ���� ��' �ް�����Ʈ���� ����Ʈ��
 ******************************************************************************/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

public class HttpPostFileParser {
	private static final boolean RETURN_NULL = true; // return null or "" for

	private static final boolean MKDIR_IF_DIR_NOT_EXIST = true;

	private static final int DEFAULT_MAX_POST_SIZE = 1 * 1024 * 1024;

	private static final String TEMP_PATH = "./ioitmpfiles/";

	public UpFile[] upFile;

	public int nFile = 0;

	private String instanceID;

	private Vector<UpFile> vUpFile;

	private ServletRequest req;

	private int maxSize;

	private Hashtable<String, String> parameters = new Hashtable<String, String>();

	private int nFileFull = 0;

	public HttpPostFileParser() {
		instanceID = String.valueOf(this.hashCode());
	}

	public void finalize() throws IOException {
	    //rado: overriding finalize for cleaning temp files is plain stupid
	    //we need to figure out another way for doing this
	    
		//clearTempDir();
	}

	public void init(ServletRequest request) throws IOException {
		init(request, DEFAULT_MAX_POST_SIZE);
	}

	public void init(ServletRequest request, int maxPostSize)
			throws IOException {
		req = request;
		maxSize = maxPostSize;

		/*
		 * ServletInputStream in = req.getInputStream(); FileOutputStream fos =
		 * new FileOutputStream("multipartrequest.txt"); byte[] b = new
		 * byte[1024]; int nRead = in.readLine(b, 0, b.length); while (nRead >
		 * 0) { fos.write(b, 0, nRead); nRead = in.readLine(b, 0, b.length); }
		 * fos.close();
		 */

		try {
			readRequest();
		} catch (IOException e) {
			clearTempDir();
			throw e;
		}
	}

	public Enumeration<String> getParameterNames() {
		return parameters.keys();
	}

	public String getParameter(String name) {
		try {
			String param = (String) parameters.get(name);
			if (param.equals("")) {
				if (RETURN_NULL == true)
					return null;
				else
					return " ";
			}
			return param;
		} catch (Exception e) {
			if (RETURN_NULL == true)
				return null;
			else
				return " ";
		}
	}

	protected void readRequest() throws IOException {
		String type = req.getContentType();
		if (type == null
				|| !type.toLowerCase().startsWith("multipart/form-data")) {
			throw new IOException(
					"HttpPostFileParser: Posted content type isn't multipart/form-data");
		}

		int length = req.getContentLength();
		if (length > maxSize) {
			throw new UploadTooBigException(String.valueOf(maxSize));
		}

		String boundary = extractBoundary(type);
		if (boundary == null) {
			throw new IOException(
					"HttpPostFileParser: Separation boundary was not specified");
		}

		MultipartInputStreamHandler in = new MultipartInputStreamHandler(req
				.getInputStream(), boundary, length);

		String line = in.readLine();
		if (line == null) {
			throw new IOException(
					"HttpPostFileParser: Corrupt form data: premature ending");
		}

		if (!line.startsWith(boundary)) {
			throw new IOException(
					"HttpPostFileParser: Corrupt form data: no leading boundary");
		}

		nFile = 0;
		nFileFull = 0;
		vUpFile = new Vector<UpFile>();
		boolean done = false;
		while (!done) {
			done = readNextPart(in, boundary);
		}
		if (nFile != vUpFile.size())
			throw new IOException(
					"HttpPostFileParser: Fatal Error: nFile != vUpFile.size()");
		upFile = new UpFile[nFile];
		vUpFile.copyInto(upFile);
	}

	protected boolean readNextPart(MultipartInputStreamHandler in,
			String boundary) throws IOException {
		// content-disposition: form-data; name="field1"; filename="file1.txt"
		String line = in.readLine();
		if (line == null) {
			return true;
		}
		if (line.trim().equals(""))
			return true;
		// if (line.trim().equals("")) return false;

		// content-disposition line
		String[] dispInfo = extractDispositionInfo(line);
		String name = dispInfo[1];
		String filename = null;
		if (dispInfo[2] != null)
			filename = dispInfo[2];

		// Content-Type empty
		line = in.readLine();
		if (line == null) {
			return true;
		}

		// Content-Type;
		String contentType = extractContentType(line);
		if (contentType != null) {
			line = in.readLine(); // Eat the empty line
			if (line == null || line.length() > 0) {
				throw new IOException(
						"HttpPostFileParser: Malformed line after content type: "
								+ line);
			}
		} else {
			contentType = "application/octet-stream"; // default content type
		}

		if (filename == null) {
			String value = readParameter(in, boundary);
			parameters.put(name, value);
		} else {
			if (!filename.equals("")) {
				UpFile tmp = new UpFile(in, boundary, filename, contentType,
						instanceID);
				tmp.fullIndex = nFileFull;
				vUpFile.addElement(tmp);
				nFile++;
				nFileFull++;
			} else {
				@SuppressWarnings("unused")
				String foo = readParameter(in, boundary);
				nFileFull++;
			}
		}
		return false; // done = false
	}

	protected String readParameter(MultipartInputStreamHandler in,
			String boundary) throws IOException {
		StringBuffer sbuf = new StringBuffer();
		String line;

		while ((line = in.readLine()) != null) {
			if (line.startsWith(boundary))
				break;
			sbuf.append(line + "\r\n"); // add the \r\n in case there are many
			// lines
		}

		if (sbuf.length() == 0) {
			return null; // nothing read
		}

		sbuf.setLength(sbuf.length() - 2); // cut off the last line's \r\n
		return sbuf.toString(); // no URL decoding needed
	}

	private String extractBoundary(String line) {
		int index = line.indexOf("boundary=");
		if (index == -1) {
			return null;
		}
		String boundary = line.substring(index + 9); // 9 for "boundary="

		// The real boundary is always preceeded by an extra "--"
		boundary = "--" + boundary;

		return boundary;
	}

	// content-disposition, name, filename;
	private String[] extractDispositionInfo(String line) throws IOException {
		String[] retval = new String[3];

		String origline = line;
		line = origline.toLowerCase();

		// content disposition; "form-data"
		int start = line.indexOf("content-disposition: "); // "content-disposition:
		// ".length == 21
		int end = line.indexOf(";");
		if (start == -1 || end == -1) {
			throw new IOException("Content disposition corrupt1: " + origline);
		}
		String disposition = line.substring(start + 21, end);
		if (!disposition.equals("form-data")) {
			throw new IOException("Invalid content disposition: " + disposition);
		}

		// field name;
		start = line.indexOf("name=\"", end);
		end = line.indexOf("\"", start + 7);
		if (start == -1 || end == -1) {
			throw new IOException("Content disposition corrupt2: " + origline);
		}
		String name = origline.substring(start + 6, end);

		// filename;
		String filename = null;
		start = line.indexOf("filename=\"", end + 2);
		end = line.indexOf("\"", start + 10);
		if (start != -1 && end != -1) {
			filename = origline.substring(start + 10, end);
			// file path�� ��8�� filename�� ����
			int slash = Math.max(filename.lastIndexOf('/'), filename
					.lastIndexOf('\\'));
			if (slash > -1) {
				filename = filename.substring(slash + 1);
			}
			// if (filename.equals("")) filename = "unknown";
		}

		retval[0] = disposition;
		retval[1] = name;
		retval[2] = filename;
		return retval;
	}

	private String extractContentType(String line) throws IOException {
		String contentType = null;

		String origline = line;
		line = origline.toLowerCase();

		if (line.startsWith("content-type")) {
			int start = line.indexOf(" ");
			if (start == -1) {
				throw new IOException("Content type corrupt: " + origline);
			}
			contentType = line.substring(start + 1);
		} else {
			if (line.length() != 0) { // content type
				throw new IOException("Malformed line after disposition: "
						+ origline);
			}
		}

		return contentType;
	}

	protected void clearTempDir() {
		System.out.println("deleting..." + TEMP_PATH + instanceID);
		File dir = new File(TEMP_PATH + instanceID); // �ӽ� ���丮
		if (dir.exists()) {
			String[] files = dir.list();
			if (files != null)
				for (int i = 0; i < files.length; i++) {
					File file = new File(dir, files[i]);
					if (file.exists())
						file.delete();
				}
			dir.delete();
		}
	}

	/*
	 * NESTED CLASSES
	 * 
	 * 
	 * 
	 */

	public class UpFile {
		public String pc_file_name = "";

		public int size = 0;

		public String file_size = "";

		public String extension = "";

		public int fullIndex = 0;

		private File tmp_file;

		private String tmp_filename = String.valueOf(this.hashCode());

		public File GetTmpFile() {
			return tmp_file;
		}

		UpFile(MultipartInputStreamHandler in, String boundary,
				String filename, String type, String tmp_dir)
				throws IOException {
			this.pc_file_name = filename.trim();
			if (this.pc_file_name.lastIndexOf(".") > -1)
				this.extension = this.pc_file_name.substring(this.pc_file_name
						.lastIndexOf("."));
			byte[] bbuf = new byte[16 * 1024]; // 16K
			int result;
			String line;

			File dir = new File(TEMP_PATH + tmp_dir);
			dir.mkdirs();
			tmp_file = new File(dir, tmp_filename);
			BufferedOutputStream out = new BufferedOutputStream(
					new FileOutputStream(tmp_file), bbuf.length);

			boolean rnflag = false;
			while ((result = in.readLine(bbuf, 0, bbuf.length)) != -1) {
				// Check for boundary
				if (result > 2 && bbuf[0] == '-' && bbuf[1] == '-') { // quick
					// pre-check
					line = new String(bbuf, 0, result, "ISO-8859-1");
					if (line.startsWith(boundary))
						break;
				}
				// Are we supposed to write \r\n for the last iteration?
				if (rnflag) {
					out.write('\r');
					out.write('\n');
					rnflag = false;
				}
				// Write the buffer, postpone any ending \r\n
				if (result >= 2 && bbuf[result - 2] == '\r'
						&& bbuf[result - 1] == '\n') {
					out.write(bbuf, 0, result - 2); // skip the last 2 chars
					rnflag = true; // make a note to write them on the next
					// iteration
				} else {
					out.write(bbuf, 0, result);
				}
			}
			out.flush();
			out.close();
			this.size = (int) tmp_file.length();
			this.file_size = String.valueOf(this.size);
		}

		public void save(String path, String filename) throws IOException {
			File fDir = new File(path);
			if (MKDIR_IF_DIR_NOT_EXIST) {
				fDir.mkdirs();
			}
			File f = new File(fDir, filename);
			f.delete();
			if (!tmp_file.renameTo(f))
				throw new UploadFileSaveException("�ӽ� ����� "
						+ tmp_file.toString() + "; " + f.toString()
						+ "�� �ű� �� ��4ϴ�");
			tmp_file = null;
			f = null;
		}
	}

	// ServletInputStream multipart/form-data
	class MultipartInputStreamHandler {
		ServletInputStream in;

		String boundary;

		int totalExpected;

		int totalRead = 0;

		byte[] buf = new byte[8 * 1024];

		public MultipartInputStreamHandler(ServletInputStream in,
				String boundary, int totalExpected) {
			this.in = in;
			this.boundary = boundary;
			this.totalExpected = totalExpected;
		}

		public String readLine() throws IOException {
			StringBuffer sbuf = new StringBuffer();
			int result;
			do {
				result = this.readLine(buf, 0, buf.length);
				if (result != -1) {
					String enc = "ISO-8859-1";
					sbuf.append(new String(buf, 0, result, enc));
				}
			} while (result == buf.length);

			if (sbuf.length() == 0) {
				return null;
			}

			sbuf.setLength(sbuf.length() - 2); // \r\n;
			return sbuf.toString();
		}

		public int readLine(byte b[], int off, int len) throws IOException {
			if (totalRead >= totalExpected)
				return -1;
			else {
				int result = in.readLine(b, off, len);
				if (result > 0)
					totalRead += result;
				return result;
			}
		}
	}

	public class UploadTooBigException extends java.io.IOException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2146694518505638742L;

		public UploadTooBigException(String s) {
			super(s);
		}
	}

	public class UploadFileSaveException extends java.io.IOException {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1317985923514348012L;

		public UploadFileSaveException(String s) {
			super(s);
		}
	}

}
