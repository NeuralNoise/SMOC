/**
 * 
 */
package bg.smoc.web.servlet.judge.contest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.TestGroup;
import bg.smoc.model.manager.ContestManager;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author zbogi
 * 
 */
public class UploadTestDataServlet extends HttpServlet {

    /**
     * As this is data submitted by judges we expect it to be quite large and
     * non-malicious in nature.
     */
    private static final int MAX_TEST_DATA_FILE_SIZE = 100 * 1024 * 1024;

    /**
     * .
     */
    private static final long serialVersionUID = -7141416388849135359L;

    @SuppressWarnings("unchecked")
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ServletFileUpload servletFileUpload = setUpServletFileUpload();
        ContestManager contestManager = SessionUtil.getInstance().getContestManager();

        List<FileItem> fileItemsList = null;
        try {
            fileItemsList = servletFileUpload.parseRequest(request);
        } catch (FileUploadException e) {
            setError(request,
                    response,
                    "File upload did not finish successfully.  Or you might be using an unsupported browser.");
            return;
        }

        InputStream inputStream = null;
        Map<String, String> fieldValues = new HashMap<String, String>();
        for (FileItem fileItem : fileItemsList) {
            if (fileItem.isFormField()) {
                fieldValues.put(fileItem.getFieldName(), fileItem.getString());
            } else {
                inputStream = fileItem.getInputStream();
                break;
            }
        }
        if (inputStream == null) {
            setError(request, response, "No file seems to be uploaded.");
            return;
        }
        Task task = getTastInfo(fieldValues, contestManager);
        if (task == null) {
            setError(request, response, "Error reading task infomation.");
            return;
        }
        try {
            List<String> messages = unzipStream(request,
                    response,
                    inputStream,
                    task,
                    contestManager,
                    fieldValues.get("contestId"));
            request.getSession().setAttribute("messages", messages);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
        response.sendRedirect("editTask?contestId="
                + fieldValues.get("contestId")
                + "&taskId="
                + task.getId());
    }

    private Task getTastInfo(Map<String, String> fieldValues, ContestManager contestManager) {
        if (fieldValues.get("contestId") == null || fieldValues.get("taskId") == null)
            return null;
        Contest contest = contestManager.getContest(fieldValues.get("contestId"));
        if (contest == null) {
            return null;
        }
        return contest.getTaskById(fieldValues.get("taskId"));
    }

    private ServletFileUpload setUpServletFileUpload() {
        ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
        servletFileUpload.setFileSizeMax(MAX_TEST_DATA_FILE_SIZE);
        return servletFileUpload;
    }

    List<String> unzipStream(HttpServletRequest request, HttpServletResponse response,
            InputStream inputStream, Task task, ContestManager contestManager, String contestId) {
        LinkedList<String> errorMessages = new LinkedList<String>();

        boolean foundMetaInfo = false;
        ZipInputStream zipStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = null;
        try {
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory())
                    continue;
                System.out.println("Unzipping " + zipEntry.getName());
                if (handleMetaInfo(zipEntry,
                        task,
                        zipStream,
                        errorMessages,
                        contestManager,
                        contestId)) {
                    if (foundMetaInfo) {
                        errorMessages.add("Found more than one file named "
                                + task.getName()
                                + ".txt . Will be using + "
                                + zipEntry.getName());
                    }
                    foundMetaInfo = true;
                } else {
                    String message = parseZipEntry(zipEntry,
                            zipStream,
                            task,
                            contestId,
                            contestManager);
                    if (message != null) {
                        errorMessages.add(message);
                    }
                }
            }
        } catch (IOException e) {
            errorMessages.add("Could not read next entry from zip file");
        }
        if (!foundMetaInfo) {
            errorMessages
                    .add("Could not find valid file named "
                            + task.getName()
                            + ".txt . Will use default settings - all test cases form separate test groups and the first 20% of the test groups will be feedback enabled.");
            setUpDefaultMetaInfo(task, contestManager, contestId);
        }
        return errorMessages;
    }

    private boolean handleMetaInfo(ZipEntry zipEntry, Task task, ZipInputStream zipStream,
            List<String> errorMessages, ContestManager contestManager, String contestId) {
        String fileName = new File(zipEntry.getName()).getName().toLowerCase();
        if (!fileName.equals(task.getName() + ".txt"))
            return false;

        List<TestGroup> testGroups = new LinkedList<TestGroup>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipStream));
        String line = null;
        int lineNumber = 0;
        try {
            while ((line = reader.readLine()) != null) {
                String secondLine = reader.readLine();
                lineNumber += 2;
                if (secondLine == null) {
                    if ("".equals(line)) {
                        break;
                    } else {
                        addMessageOnLine(errorMessages, lineNumber, "Line appears to be empty.");
                        return false;
                    }
                }

                TestGroup group = new TestGroup();
                if (!parseOddLineOfMetaInfo(errorMessages, line, lineNumber, group)
                        || !parseEvenLineOfMetaInfo(secondLine,
                                task,
                                errorMessages,
                                lineNumber,
                                group)) {
                    return false;
                }
                testGroups.add(group);
            }
        } catch (IOException e) {
            addMessageOnLine(errorMessages, lineNumber, "Cannot read the file after this line.");
            return false;
        }
        task.setTestGroups(testGroups);
        contestManager.updateTask(contestId, task);

        return true;
    }

    private boolean parseEvenLineOfMetaInfo(String line, Task task, List<String> errorMessages,
            int lineNumber, TestGroup group) {
        String[] testIndexes = line.split(" ");
        List<Integer> testsCases = new LinkedList<Integer>();
        for (String index : testIndexes) {
            try {
                int test = Integer.parseInt(index);
                if (test <= 0 || test > task.getNumberOfTests()) {
                    addMessageOnLine(errorMessages, lineNumber, "Incorrect index "
                            + index
                            + " must be between 1 and "
                            + task.getNumberOfTests());
                    return false;
                }
                testsCases.add(test);
            } catch (NumberFormatException e) {
                addMessageOnLine(errorMessages, lineNumber, "Index " + index + " is not a number.");
                return false;
            }
        }
        if (testsCases.isEmpty()) {
            addMessageOnLine(errorMessages,
                    lineNumber,
                    "Cannot create a test group with no test cases.");
            return false;
        }
        group.setTestCases(testsCases);
        return true;
    }

    private boolean parseOddLineOfMetaInfo(List<String> errorMessages, String line, int lineNumber,
            TestGroup group) {
        String[] firstLine = line.split(" ");
        if (firstLine.length != 2) {
            addMessageOnLine(errorMessages, lineNumber - 1, "Line has incorrect format. "
                    + "Should consist of the number of points for the test case and "
                    + "yes/no if feedback is enabled separated by space"
                    + " e.g. '10 yes'.");
            return false;
        }
        try {
            group.setPoints(new BigDecimal(firstLine[0]));
        } catch (NumberFormatException e) {
            addMessageOnLine(errorMessages,
                    lineNumber - 1,
                    "The line does not start with an integer/double");
            return false;
        }
        if ("yes".equalsIgnoreCase(firstLine[1])) {
            group.setFeedbackEnabled(true);
        } else if ("no".equalsIgnoreCase(firstLine[1])) {
            group.setFeedbackEnabled(false);
        } else {
            addMessageOnLine(errorMessages,
                    lineNumber - 1,
                    "The string for feedback is neither 'yes' nor 'no'.");
            return false;
        }
        return true;
    }

    private void addMessageOnLine(List<String> errorMessages, int lineNumber, String message) {
        errorMessages.add("Error while parsing test grouping info on line "
                + lineNumber
                + " : "
                + message);
    }

    private void setUpDefaultMetaInfo(Task task, ContestManager contestManager, String contestId) {
        task.setTestGroups(new LinkedList<TestGroup>());
        for (int i = 1; i <= task.getNumberOfTests(); ++i) {
            TestGroup group = new TestGroup();
            group.setFeedbackEnabled(i * 5 <= task.getNumberOfTests());
            group.setTestCases(Arrays.asList(i));
            group.setPoints(new BigDecimal(100 / task.getNumberOfTests()));
            task.addTestGroup(group);
        }
        contestManager.updateTask(contestId, task);
    }

    protected String parseZipEntry(ZipEntry zipEntry, ZipInputStream zipStream, Task task,
            String contestId, ContestManager contestManager) {
        String fileName = new File(zipEntry.getName()).getName().toLowerCase();
        if (fileName.equals(task.getName() + ".txt")) {
            return null;
        }

        String[] fileNameParts = fileName.split("\\.");

        if (fileNameParts.length != 3) {
            return getErrorMessage(zipEntry, "is not named properly and will be ignored.");
        }
        if (!fileNameParts[0].equals(task.getName())) {
            return getErrorMessage(zipEntry,
                    "seems like a test case but does not seem to start with the task name:"
                            + task.getName());
        }
        Task.TestType testType;
        if ("in".equals(fileNameParts[2])) {
            testType = Task.TestType.IN;
        } else if ("sol".equals(fileNameParts[2])) {
            testType = Task.TestType.SOL;
        } else {
            return getErrorMessage(zipEntry, " is neither .in nor .sol file but is ."
                    + fileNameParts[2]
                    + " ?");
        }

        int testNumber = -1;
        try {
            testNumber = Integer.parseInt(fileNameParts[1]);
            if (testNumber < 0 || testNumber > task.getNumberOfTests()) {
                testNumber = -1;
            }
        } catch (NumberFormatException e) {
        } finally {
            if (testNumber < 0) {
                return getErrorMessage(zipEntry, " has incorrect value for a test number :"
                        + fileNameParts[1]
                        + " . Must be an integer between 1 and "
                        + Integer.toString(task.getNumberOfTests()));
            }
        }

        if (contestManager.uploadTaskData(contestId, task, testNumber, testType, zipStream)) {
            if (zipEntry.getSize() == 0 || zipEntry.getSize() == 1) {
                return getErrorMessage(zipEntry, " has 0 or 1 bytes size. This might not be an error.");
            }
            return null;
        } else {
            return getErrorMessage(zipEntry,
                    " was recognized as a test data file but the server was unable to store it.");
        }
    }

    private String getErrorMessage(ZipEntry zipEntry, String message) {
        return "File "
                + zipEntry.getName()
                + " "
                + message
                + " Correct format is task_name.test_number.in_or_sol e.g. apple.005.in .";
    }

    /**
     * This method needs to be thread-safe.
     * 
     * @param response
     * @param message
     * @throws IOException
     */
    private void setError(HttpServletRequest request, HttpServletResponse response,
            final String message) throws IOException {
        request.getSession().setAttribute("uploadMessage", message);
        response.sendRedirect("");
    }

}
