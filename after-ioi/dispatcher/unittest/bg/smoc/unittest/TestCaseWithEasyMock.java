package bg.smoc.unittest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

import org.easymock.EasyMock;
import org.junit.Test;

public class TestCaseWithEasyMock {

    LinkedList<Object> mocks;

    public <T> T createMock(Class<T> mockedClass) {
        T mock = EasyMock.createMock(mockedClass);
        mocks.add(mock);
        return mock;
    }

    public void replayAll() {
        for (Object mock : mocks) {
            EasyMock.replay(mock);
        }
    }

    @Test
    public void runTest() throws Throwable {
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EasyMockTest.class)) {
                try {
                    mocks = new LinkedList<Object>();
                    method.invoke(this);
                } catch (InvocationTargetException e) {
                    if (e.getCause() != null) {
                        throw e.getCause();
                    }
                }
            }
        }
    }
}
