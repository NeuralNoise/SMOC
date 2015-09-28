package bg.smoc.model;

import java.util.Vector;

public class Person {

    private String names;

    private String town;

    private int schoolYear;

    private String school;

    private Vector<String> logins;

    private String id;

    private String email;

    public Vector<String> getLogins() {
        if (logins == null)
            logins = new Vector<String>();
        return logins;
    }

    public void setLogins(Vector<String> logins) {
        this.logins = logins;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public int getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(int schoolYear) {
        this.schoolYear = schoolYear;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void populateFrom(Person person) {
        this.logins = person.logins;
        this.names = person.names;
        this.school = person.school;
        this.schoolYear = person.schoolYear;
        this.town = person.town;
        this.email = person.email;
    }

    public static Person getBlankPerson(String login) {
        Person result = new Person();
        result.setNames("");
        result.setTown("");
        result.setLogins(new Vector<String>());
        result.getLogins().add(login);
        return result;
    }
}
