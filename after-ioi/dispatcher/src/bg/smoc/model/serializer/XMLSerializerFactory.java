package bg.smoc.model.serializer;

public class XMLSerializerFactory implements SerializerFactory {
    
    private String workingDirectory;

    public XMLSerializerFactory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ContestSerializer createContestSerializer() {
        XMLContestSerializer contestSerializer = new XMLContestSerializer(workingDirectory);
        contestSerializer.init();
        return contestSerializer;
    }

    public PersonSerializer createPersonSerializer() {
        XMLPersonSerializer instance = new XMLPersonSerializer(workingDirectory);
        instance.init();
        return instance;
    }

    public UserAccountSerializer createUserAccountSerializer() {
        XMLUserAccountSerializer instance = new XMLUserAccountSerializer(workingDirectory);
        instance.init();
        return instance;
    }

}
