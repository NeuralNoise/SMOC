package bg.smoc.model.serializer;

public interface SerializerFactory {
    public ContestSerializer createContestSerializer();

    public PersonSerializer createPersonSerializer();

    public UserAccountSerializer createUserAccountSerializer();
}
