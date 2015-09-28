package bg.smoc.model.serializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import bg.smoc.model.Person;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XMLPersonSerializer implements PersonSerializer{

    private static final String PERSONS_XML_FILENAME = "persons.xml";

    Vector<Person> persons;

    private File workingFile;

    public XMLPersonSerializer(String workingDirectory) {
        workingFile = new File(workingDirectory, PERSONS_XML_FILENAME);
    }

    @SuppressWarnings("unchecked")
    public void init() {
        persons = new Vector<Person>();
        XStream xstream = new XStream(new DomDriver());
        try {
            FileInputStream fileStream = new FileInputStream(workingFile);
            Object serializedPersons = xstream.fromXML(fileStream);
            if (serializedPersons != null) {
                persons = (Vector<Person>) serializedPersons;
            }
            fileStream.close();
        } catch (IOException e) {
            return;
        }
        ValidateConsitency();
    }

    public void addPerson(Person newPerson) {
        Person storePerson = new Person();
        storePerson.populateFrom(newPerson);
        storePerson.setId(getNextId());
        persons.add(storePerson);
        store();
    }

    synchronized private void store() {
        XStream xstream = new XStream(new DomDriver());
        try {
            FileOutputStream fileStream = new FileOutputStream(workingFile);
            xstream.toXML(persons, fileStream);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized private String getNextId() {
        long nextAvailableId = 1;
        while (true) {
            String nextId = Long.toString(nextAvailableId);
            boolean okToUse = true;
            for (Person person : persons) {
                if (nextId.equals(person.getId())) {
                    okToUse = false;
                    break;
                }
            }
            if (okToUse)
                return nextId;
            nextAvailableId++;
        }
    }

    private void ValidateConsitency() {
        for (int i = 0; i < persons.size(); ++i) {
            String personId = persons.get(i).getId();
            if (personId == null) {
                persons.remove(i);
                --i;
            }
            for (int j = i + 1; j < persons.size(); ++j) {
                if (personId.equals(persons.get(j).getId())) {
                    persons.remove(j);
                    --j;
                }
            }
        }
    }

    public Vector<Person> getPersons() {
        return persons;
    }

    public void deletePerson(String personId) {
        if (personId == null)
            return;

        for (int i = 0; i < persons.size(); ++i) {
            if (personId.equals(persons.get(i).getId())) {
                persons.remove(i);
                --i;
            }
        }
    }

    public Person getPersonById(String personId) {
        if (personId == null) {
            return null;
        }
        for (Person person : persons) {
            if (personId.equals(person.getId()))
                return person;
        }
        return null;
    }

    public void update(Person modifiedPerson) {
        Person storePerson = getPersonById(modifiedPerson.getId());
        if (storePerson == null)
            return;

        storePerson.populateFrom(modifiedPerson);
        store();
    }
}
