package bg.smoc.web.servlet;

import bg.smoc.model.Contest;
import bg.smoc.web.servlet.contest.ContestPropertyServlet;

public class SetTestingAllowedServlet extends ContestPropertyServlet {

	private static final long serialVersionUID = -5390064906133463505L;

    @Override
    public void updateContestProperty(Contest contest, boolean value) {
        contest.setTestingOn(value);
    }

}
