package bg.smoc.web.utils;

import javax.servlet.ServletContext;

import bg.smoc.model.manager.ContestManager;
import bg.smoc.model.manager.GraderManager;
import bg.smoc.model.manager.LoginManager;
import bg.smoc.model.manager.ManagerMediator;
import bg.smoc.model.manager.PersonManager;
import bg.smoc.model.manager.PrintManager;
import bg.smoc.model.manager.UserAccountManager;
import bg.smoc.model.serializer.ContestSerializer;
import bg.smoc.model.serializer.PersonSerializer;
import bg.smoc.model.serializer.SerializerFactory;
import bg.smoc.model.serializer.UserAccountSerializer;

public class SessionUtil {
    private static SessionUtil instance;

    private LoginManager loginManager;

    private UserAccountManager userAccountManager;

    private ContestManager contestManager;

    private GraderManager graderManager;

    private PersonManager personManager;
    
    private PrintManager printManager;

    private ContestSerializer contestSerializer;

    private PersonSerializer personSerializer;
    
    private UserAccountSerializer userAccountSerializer;

    private String workingDirectory;

    private int gradingPort;

    private SerializerFactory serializerFactory;

    private ManagerMediator mediator;

    private SessionUtil() {
    }

    /**
     * Returns and instance of the SessionUtil and from there all managers.
     * However for it to function properly initialize must be called at least
     * once.
     * 
     * @return the SessionUtil singleton
     */
    synchronized public static SessionUtil getInstance() {
        if (instance == null) {
            instance = new SessionUtil();
        }
        return instance;
    }

    public LoginManager getLoginManager() {
        return loginManager;
    }

    public UserAccountManager getUserAccountManager() {
        return userAccountManager;
    }

    public ContestManager getContestManager() {
        return contestManager;
    }

    public GraderManager getGraderManager() {
        return graderManager;
    }

    public PersonManager getPersonManager() {
        return personManager;
    }
    
    public PrintManager getPrintManager() {
        return printManager;
    }

    private void initiateManagers() {
        setUpUserAccountSerializer();
        setUpContestSerializer();
        setUpPersonSerializer();

        setUpManagers();
    }

    /**
     * Creates, initializes and stores to the application context the
     * UserAccountManager, LoginManager, ContestManager, PersonManager and
     * GraderManager.
     */
    private void setUpManagers() {
        userAccountManager = new UserAccountManager(userAccountSerializer);
        loginManager = new LoginManager();
        personManager = new PersonManager(personSerializer);
        graderManager = new GraderManager(gradingPort, workingDirectory);
        contestManager = new ContestManager(workingDirectory, contestSerializer);
        printManager = new PrintManager(workingDirectory);
        
        mediator = new ManagerMediator();
        mediator.setManagersFromSessionUtil(this);

        contestManager.initActiveUsers();
        // schedule all running contests for termination
        contestManager.scheduleRunningContests();
    }

    /**
     * Creates, initializes the UserAccountSerializer and stores it to the
     * applicationContext.
     */
    private void setUpUserAccountSerializer() {
        userAccountSerializer = serializerFactory.createUserAccountSerializer();
    }

    /**
     * Creates, initializes the ContestSerializer and stores it to the
     * applicationContext.
     */
    private void setUpContestSerializer() {
        contestSerializer = serializerFactory.createContestSerializer();
    }

    /**
     * Creates, initializes the PersonSerializer and stores it to the
     * applicationContext.
     */
    private void setUpPersonSerializer() {
        personSerializer = serializerFactory.createPersonSerializer();
    }

    /**
     * Creates a SessionUtil that can be used to access different application
     * scope managers.
     * 
     * @param servletContext
     *            the servletContext where the Managers and Serializers are
     *            stored. It must remain valid for the period of use of the
     *            SessionUtil object
     * @return an active SessionUtil object
     */

    /**
     * Initializes the SessionUtil to a given servletContext. This method must
     * be called once at the application startup.
     * 
     * @param servletContext
     *            the context in which the managers and serializers are
     *            applicable
     */
    public void initialize(ServletContext servletContext, SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;

        workingDirectory = servletContext.getRealPath("/judge/storage/");
        gradingPort = Integer.parseInt(servletContext.getInitParameter("gradingPort"));
        initiateManagers();
    }

}
