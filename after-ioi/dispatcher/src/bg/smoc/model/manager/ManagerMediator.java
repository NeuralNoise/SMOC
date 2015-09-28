package bg.smoc.model.manager;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import kr.or.ioi2002.RMIServer.TempFile;
import kr.or.ioi2002.RMIServer.User;
import bg.smoc.model.Contest;
import bg.smoc.model.Task;
import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.SessionUtil;

public class ManagerMediator {

    private UserAccountManager userAccountManager;
    private LoginManager loginManager;
    private ContestManager contestManager;
    private GraderManager graderManager;
    private PersonManager personManager;

    public void setManagersFromSessionUtil(SessionUtil sessionUtil) {
        userAccountManager = sessionUtil.getUserAccountManager();
        userAccountManager.setMediator(this);
        loginManager = sessionUtil.getLoginManager();
        loginManager.setMediator(this);
        contestManager = sessionUtil.getContestManager();
        contestManager.setMediator(this);
        graderManager = sessionUtil.getGraderManager();
        graderManager.setMediator(this);
        personManager = sessionUtil.getPersonManager();
        personManager.setMediator(this);
    }

    public boolean verifyUserPassword(String login, String passwordHash) {
        UserAccount account = userAccountManager.getUserAccount(login);
        return (account != null && passwordHash.equals(account.getPasswordHash()));
    }

    public void registerUserForContest(String contestId, String username) {
        contestManager.registerUserForContest(contestId, username);
    }

    public void removeUser(String login) {
        contestManager.removeUser(login);
    }

    public void test(String id, String userId, String task, String language, TempFile tmp,
            TempFile tmp2, User user) {
        graderManager.test(id, userId, task, language, tmp, tmp2, user.getSubmitState(task));
    }

    public void submit(String id, String userId, Task task, String language, TempFile tmp,
            String sourceFileName, User user, boolean isAlwaysAccept) {
        graderManager.submit(id, userId, task, language, tmp, sourceFileName, user
                .getSubmitState(task.getName()), isAlwaysAccept);
    }

    public Vector<UserAccount> getAllUsers() {
        return userAccountManager.getAllUsers();
    }

    public File getSourceCode(Contest contest, String login, String taskName) {
        return contestManager.getSourceCode(contest, login, taskName);
    }

    public File getSourceCode(String contestId, String userId, String task) {
        return contestManager.getSourceCode(contestId, userId, task);
    }

    public String getSourceCodeLanguage(String contest, String userid, String task) {
        return contestManager.getSourceCodeLanguage(contest, userid, task);
    }

    public boolean getContestHasTask(String contestId, String taskName) {
        Contest contest = contestManager.getContest(contestId);
        if (contest == null)
            return false;
        return contest.hasTaskNamed(taskName);
    }

    public boolean submitSourceCode(String contestId, String userid, String task, TempFile tmpsrc,
            String language, String srcFilename, String output) throws IOException {
        User user = contestManager.getUser(contestId, userid);
        if (user == null)
            return false;

        return user.submitSourceCode(task, tmpsrc, language, srcFilename, output);
    }

    public boolean submitAttemptFailed(String contestId, String userid, String task, String output) {
        User user = contestManager.getUser(contestId, userid);
        if (user == null)
            return false;

        return user.submitAttemptFailed(task, output);
    }

    public boolean testFinish(String contestId, String userid, String task, String output) {
        User user = contestManager.getUser(contestId, userid);
        if (user == null)
            return false;

        return user.getTestState().finished(output);
    }

    public ContestManager getContestManager() {
        return contestManager;
    }
}
