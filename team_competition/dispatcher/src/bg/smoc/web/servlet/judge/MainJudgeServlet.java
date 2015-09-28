/**
 * 
 */
package bg.smoc.web.servlet.judge;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import bg.smoc.model.manager.GraderManager;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class MainJudgeServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -212249266673072996L;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		GraderManager graderManager = SessionUtil.getInstance().getGraderManager();
		attachMachineQueue(request, "busyQueue", graderManager.getBusyMachineQueue());
		attachMachineQueue(request, "idleQueue", graderManager.getIdleMachineQueue());
		request.setAttribute("submitQueue", graderManager.getSubmitQueue());
        request.setAttribute("feedbackQueue", graderManager.getFeedbackQueue());
		request.setAttribute("testQueue", graderManager.getTestQueue());
		request.setAttribute("gradeQueue", graderManager.getGradeQueue());

		request.setAttribute("autoreload", request.getParameter("autoreload"));
		loadSystemTime(request);

		request.getRequestDispatcher("main.jsp").forward(request, response);
	}

	private void attachMachineQueue(HttpServletRequest request, String attributeName, String[] machineQueue) {
		List<String[]> tokenizedQueue = new ArrayList<String[]>();
		
		for (String machineInfo : machineQueue)
			tokenizedQueue.add(machineInfo.split(",", 5));

		request.setAttribute(attributeName, tokenizedQueue);
	}

	/**
	 * Sets the system time as dateTimeNow attribute of the request.
	 * 
	 * @param request
	 *            the request where "now" has to be attached
	 */
	private void loadSystemTime(HttpServletRequest request) {
		request.setAttribute("dateTimeNow", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
	}
}
