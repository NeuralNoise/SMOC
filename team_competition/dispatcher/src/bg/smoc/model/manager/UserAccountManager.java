package bg.smoc.model.manager;

import java.util.Vector;

import bg.smoc.model.UserAccount;
import bg.smoc.model.serializer.UserAccountSerializer;

public class UserAccountManager extends GenericManager {

    private UserAccountSerializer userAccountSerializer;

    public UserAccountManager(UserAccountSerializer userAccountSerializer) {
        this.userAccountSerializer = userAccountSerializer;
    }

    public Vector<UserAccount> getAllUsers() {
        return userAccountSerializer.getAllUserAccounts();
    }

    public UserAccount createUser(UserAccount account) {
        return userAccountSerializer.createNewUser(account);
    }

    public void generateUsers(String users, String contestId) {
        for (String userLogin : users.split(" |\t|\n|\r")) {
            String username = userLogin.trim();
            if (username.equals(""))
                continue;
            userAccountSerializer.createNewUser(username, contestId);
            if (contestId != null && contestId.length() > 0)
                mediator.registerUserForContest(contestId, username);
        }
    }

    public void update(UserAccount userAccount) {
        userAccountSerializer.update(userAccount);
    }

    public UserAccount getUserAccount(String login) {
        return userAccountSerializer.getUserById(login);
    }

    public void delete(String login) {
        userAccountSerializer.delete(login);
        mediator.removeUser(login);
    }

    public void registerUserForContest(String userLogin, String contestId) {
        UserAccount account = getUserAccount(userLogin);
        account.getContestIds().add(contestId);
        update(account);
        mediator.registerUserForContest(contestId, userLogin);
    }
}