package bg.smoc.model.serializer;

import java.util.Vector;

import bg.smoc.model.UserAccount;

public interface UserAccountSerializer {
    public Vector<UserAccount> getAllUserAccounts();

    public void createNewUser(String userLogin, String contestId);

    public UserAccount createNewUser(UserAccount user);

    public UserAccount getUserById(String login);

    public void update(UserAccount userAccount);

    public void delete(String login);
}
