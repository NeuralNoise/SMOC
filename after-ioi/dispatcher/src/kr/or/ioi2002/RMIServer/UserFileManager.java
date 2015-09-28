/*
 * Copyright 2002 HM Research, Ltd. All rights reserved.
 */

package kr.or.ioi2002.RMIServer;

/**
 * 
 * @author Sunglim Lee
 * @version 1.00, 11/01/03
 */

import java.io.*;
import java.util.Hashtable;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class UserFileManager {
    private static final String DIR_SUBMIT = "SUBMIT";

    private static final String PROPERTY_FILENAME = "submit_properties";

    private File userpath = null;

    private File submitpath = null;

    private File propertyfile = null;

    private Hashtable<String, String> properties = null;

    public UserFileManager(String contestId, String userid, String workingDirectory) {
        userpath = new File(new File(new File(workingDirectory, "USERS"), contestId), userid);
        userpath.mkdirs(); // make user dir
        submitpath = new File(userpath, DIR_SUBMIT);
        submitpath.mkdirs(); // make submit dir

        propertyfile = new File(userpath, PROPERTY_FILENAME);

        properties = new Hashtable<String, String>();
        loadStatus();
    }

    /**
     * return copy of properties
     */
    public Hashtable<String, String> getStatus() {
        return new Hashtable<String, String>(properties);
    }

    /**
     * return null if not found
     */
    public File getSourceCode(String task) {
        String filename = properties.get(task);
        if (filename == null)
            return null; // not found
        File file = new File(submitpath, filename);
        if (!file.exists())
            return null; // not found

        return file;
    }
    
    public String getSourceCodeLanguage(String task) {
    	return properties.get(task + "_language");
    }

    public boolean submitSourceCode(String task, TempFile tmp, String language,
            String srcFilename, java.util.Date dateSubmit) throws IOException {
        // add new submit file information
        int iFilename = 2001000;
        while (properties.containsValue(String.valueOf(iFilename)) != true && iFilename > 2000000)
            iFilename--;
        iFilename++;
        String strFilename = String.valueOf(iFilename);

        File file = new File(userpath + File.separator + DIR_SUBMIT + File.separator + strFilename);
        boolean bResult = tmp.makePermanent(file);

        if (bResult) {
            properties.put(task, strFilename);
            properties.put(task + "_filename", srcFilename);
            properties.put(task + "_language", language);
            String strDate = Util.DATETIME_FORMAT.format(dateSubmit);
            properties.put(task + "_submit_time", strDate);
            saveStatus();
        } else {
            Syslog.log("!submitSourceCode: tmp.makePermanent returned false: " + file.toString());
        }

        return bResult;
    }

    public boolean submitAttemptFailed(String task) {
        return true;
    }

    @SuppressWarnings("unchecked")
    private void loadStatus() {
        if (propertyfile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(propertyfile);
                XStream xstream = new XStream(new DomDriver());
                properties = (Hashtable<String, String>) xstream.fromXML(fis);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void saveStatus() throws IOException {
        if (!propertyfile.exists())
            propertyfile.createNewFile();

        FileOutputStream fos = new FileOutputStream(propertyfile);
        XStream xstream = new XStream(new DomDriver());
        xstream.toXML(properties, fos);
        fos.flush();
        fos.close();
    }
}
