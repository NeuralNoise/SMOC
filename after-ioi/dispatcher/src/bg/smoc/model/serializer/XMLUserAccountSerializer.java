package bg.smoc.model.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import bg.smoc.model.UserAccount;
import bg.smoc.web.utils.ServletUtil;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XMLUserAccountSerializer implements UserAccountSerializer {

    private static final String USER_ACCOUNTS_XML_FILENAME = "userAccounts.xml";

    private Vector<UserAccount> userAccounts;

    private File workingFile;

    public XMLUserAccountSerializer(String workingDirectory) {
        workingFile = new File(workingDirectory, USER_ACCOUNTS_XML_FILENAME);
    }

    @SuppressWarnings("unchecked")
    public void init() {
        userAccounts = new Vector<UserAccount>();
        XStream xstream = new XStream(new DomDriver());
        try {
            FileInputStream fileStream = new FileInputStream(workingFile);
            Object serializedUsers = xstream.fromXML(fileStream);
            if (serializedUsers != null) {
                userAccounts = (Vector<UserAccount>) serializedUsers;
            }
            fileStream.close();

            ValidateConsitency();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void ValidateConsitency() {
        for (int i = 0; i < userAccounts.size(); ++i) {
            String login = userAccounts.get(i).getLogin();
            if (login == null) {
                userAccounts.remove(i);
                --i;
            }
            for (int j = i + 1; j < userAccounts.size(); ++j) {
                if (login.equals(userAccounts.get(j).getLogin())) {
                    userAccounts.remove(j);
                    --j;
                }
            }
        }
    }

    public final Vector<UserAccount> getAllUserAccounts() {
        return userAccounts;
    }

    public void createNewUser(String userLogin, String contestId) {
        if (getUserById(userLogin) != null)
            return;

        UserAccount user = new UserAccount();
        user.setLogin(userLogin);
        user.setPassword(generateRandomPassword());
        user.setPasswordHash(ServletUtil.encryptPassword(user.getPassword()));
        user.getContestIds().add(contestId);
        userAccounts.add(user);
        storeUsers();
    }

    public UserAccount createNewUser(UserAccount user) {
        assert (user.getLogin().length() > 0);
        assert (user.getPasswordHash().length() > 0);
        userAccounts.add(user);
        storeUsers();
        return user;
    }

    synchronized private void storeUsers() {
        XStream xstream = new XStream(new DomDriver());
        try {
            FileOutputStream fileStream = new FileOutputStream(workingFile);
            xstream.toXML(userAccounts, fileStream);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateRandomPassword() {
        char[] good_letters = { '9', 'e', 'y', 'u', 'm', 'p', 't', 's', '2', '3', 'n', '5', '6',
                '7', '8', 'a', 'w', 'r', 'a', 'd', 'y', 'h', 'k', 'x', 'c', 'v', 'b', '4' };
        StringBuffer password = new StringBuffer("");
        for (int i = 0; i < 8; ++i)
            password.append(good_letters[(int) Math
                    .round((good_letters.length - 1) * Math.random())]);
        return password.toString();
    }

    public UserAccount getUserById(String login) {
        if (login == null)
            return null;
        for (UserAccount userAccount : userAccounts) {
            if (login.equals(userAccount.getLogin()))
                return userAccount;
        }
        return null;
    }

    public void update(UserAccount userAccount) {
        UserAccount prevUser = getUserById(userAccount.getLogin());
        if (prevUser == null) {
            System.out.println("Count not update user " + userAccount.getLogin());
            return;
        }
        prevUser.populateFrom(userAccount);
        storeUsers();
    }

    public void delete(String login) {
        if (login == null)
            return;

        for (int i = 0; i < userAccounts.size(); ++i) {
            if (login.equals(userAccounts.get(i).getLogin())) {
                userAccounts.remove(i);
                --i;
            }
        }

        storeUsers();
    }
}