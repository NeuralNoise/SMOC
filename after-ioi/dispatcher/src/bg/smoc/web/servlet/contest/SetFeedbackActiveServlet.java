package bg.smoc.web.servlet.contest;

import bg.smoc.model.Contest;

public class SetFeedbackActiveServlet extends ContestPropertyServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 5948805884062810951L;

    @Override
    public void updateContestProperty(Contest contest, boolean value) {
        contest.setFeedbackOn(value);
    }
}
