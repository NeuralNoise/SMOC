package bg.smoc.model.manager;

import java.util.Vector;

import bg.smoc.model.Person;
import bg.smoc.model.serializer.PersonSerializer;

public class PersonManager extends GenericManager {

    private PersonSerializer personSerializer;

    public PersonManager(PersonSerializer personSerializer) {
        this.personSerializer = personSerializer;
    }

    public Vector<Person> getAllPersons() {
        return personSerializer.getPersons();
    }

    public void addPerson(Person person) {
        personSerializer.addPerson(person);
    }

    public Person getPerson(String personId) {
        return personSerializer.getPersonById(personId);
    }

    public void update(Person person) {
        personSerializer.update(person);
    }

    public void delete(String personId) {
        personSerializer.deletePerson(personId);
    }

}
