package bg.smoc.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserAccount implements Serializable, Comparable<UserAccount> {

    private static final long serialVersionUID = 6853003064733966226L;

    private String login;

    private String password;
    
    private String passwordHash;

    private Set<String> contestIds = null;

    public String getLogin() {
        return login;
    }

    public void setLogin(String id) {
        this.login = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<String> getContestIds() {
        if (contestIds == null)
            contestIds = new HashSet<String>();
        return contestIds;
    }

    public void setContestIds(Set<String> contestIds) {
        this.contestIds = contestIds;
    }

    public void populateFrom(UserAccount userAccount) {
        this.login = userAccount.login;
        this.password = userAccount.password;
        this.passwordHash = userAccount.passwordHash;
        this.contestIds = userAccount.contestIds;
    }

    public int compareTo(UserAccount otherAccount) {
        return login.compareTo(otherAccount.login);
    }
}
