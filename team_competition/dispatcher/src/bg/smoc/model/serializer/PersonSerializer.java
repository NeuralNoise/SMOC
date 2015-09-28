package bg.smoc.model.serializer;

import java.util.Vector;

import bg.smoc.model.Person;

public interface PersonSerializer {
    public void addPerson(Person newPerson);

    public Vector<Person> getPersons();

    public void deletePerson(String personId);

    public Person getPersonById(String personId);

    public void update(Person modifiedPerson);
}
