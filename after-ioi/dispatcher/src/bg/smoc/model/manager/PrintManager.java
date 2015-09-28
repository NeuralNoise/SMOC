package bg.smoc.model.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

public class PrintManager {

    // keeps login -> printerName
    public final static String PRINTER_ASSIGNMENT_FILE = "printer.properties";
    // keeps login -> location
    public final static String LOCATION_FILE = "location.properties";

    PrintService[] services;
    File printerPropsFile;
    File locationPropsFile;
    Properties printerProps = new Properties();
    Properties locationProps = new Properties();

    // the maximum file size for printing in KB
    int maxFileSize = 50;

    public PrintManager(String workingDirectory) {
        DocFlavor format = DocFlavor.INPUT_STREAM.AUTOSENSE;
        services = PrintServiceLookup.lookupPrintServices(format, null);
        printerPropsFile = new File(workingDirectory, PRINTER_ASSIGNMENT_FILE);
        locationPropsFile = new File(workingDirectory, LOCATION_FILE);
    }

    public int getMaxFileSize() {
        return maxFileSize;
    }

    private void doPrint(PrintService service, File toPrintFile) throws Exception {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(toPrintFile);
            DocFlavor format = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc myDoc = new SimpleDoc(stream, format, null);
            PrintRequestAttributeSet aset = null;
            // new HashPrintRequestAttributeSet();
            // aset.add(new Copies(1));
            // aset.add(MediaSize.ISO.A4);
            // aset.add(Sides.ONE_SIDED);
            DocPrintJob job = service.createPrintJob();
            job.addPrintJobListener(new SmocPrintJobListener());
            job.print(myDoc, aset);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private void insertHeader(File toPrintFile, String login, String location) throws Exception {
        File tmpFile = new File(toPrintFile.getAbsolutePath() + ".tmp");
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            fos = new FileOutputStream(tmpFile);
            fos.write(("  LOGIN: " + login + " LOCATION: " + location + "\n\n").getBytes());
            fis = new FileInputStream(toPrintFile);
            byte[] buff = new byte[512];
            int len = 0;
            while (true) {
                len = fis.read(buff, 0, buff.length);
                if (len == -1) {
                    break;
                }
                fos.write(buff, 0, len);
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
        if (!toPrintFile.delete()) {
            throw new IOException("Cannot delete file");
        }
        if (!tmpFile.renameTo(toPrintFile)) {
            throw new IOException("Cannot rename file");
        }
    }

    public boolean print(File toPrintFile, String login) {
        return print(toPrintFile, login, true);
    }

    public boolean print(File toPrintFile, String login, boolean printHeader) {
        System.out.println("[PrintManager] file:"
                + toPrintFile.getAbsolutePath()
                + " login: "
                + login);
        try {
            FileInputStream fis = new FileInputStream(printerPropsFile);
            printerProps.load(fis);
            fis.close();
            fis = new FileInputStream(locationPropsFile);
            locationProps.load(fis);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        String printer = (String) printerProps.get(login);
        String location = (String) locationProps.get(login);
        if (printer == null) {
            System.out.println("[PrintManager] no printer assigned for login: " + login);
            return false;
        }
        if (location == null) {
            System.out.println("[PrintManager] no location assigned for login: " + login);
            return false;
        }
        if (services == null) {
            System.out.println("[PrintManager] no PrintServices found");
            return false;
        }
        for (int i = 0; i < services.length; i++) {
            // System.out.println("[PM-DEBUG] serviceName: " +
            // services[i].getName());
            if (printer.equals(services[i].getName())) {
                try {
                    if (printHeader) {
                        insertHeader(toPrintFile, login, location);
                    }
                    doPrint(services[i], toPrintFile);
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }
        System.out.println("[PrintManager] cannot find PrintService, login: "
                + login
                + " printer: "
                + printer
                + " location: "
                + location);
        return false;
    }

    private static class SmocPrintJobListener implements PrintJobListener {

        public void printDataTransferCompleted(PrintJobEvent arg0) {
        }

        public void printJobCanceled(PrintJobEvent event) {
            DocPrintJob printJob = event.getPrintJob();
            String printerName = printJob.getPrintService().getName();
            System.out.println("[PrintManager] print job canceled on: " + printerName);
        }

        public void printJobCompleted(PrintJobEvent event) {
            DocPrintJob printJob = event.getPrintJob();
            String printerName = printJob.getPrintService().getName();
            System.out.println("[PrintManager] print job completed on: " + printerName);
        }

        public void printJobFailed(PrintJobEvent event) {
            DocPrintJob printJob = event.getPrintJob();
            String printerName = printJob.getPrintService().getName();
            System.out.println("[PrintManager] print job failed on: " + printerName);
        }

        public void printJobNoMoreEvents(PrintJobEvent arg0) {
        }

        public void printJobRequiresAttention(PrintJobEvent event) {
            DocPrintJob printJob = event.getPrintJob();
            String printerName = printJob.getPrintService().getName();
            System.out.println("[PrintManager] print job requires attention on: " + printerName);
        }
    }
}
