/**
 * 
 */
package bg.smoc.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import bg.smoc.model.serializer.XMLSerializerFactory;
import bg.smoc.web.utils.SessionUtil;

/**
 * @author tsvetan.bogdanov@gmail.com
 * 
 */
public class StartUpListener implements ServletContextListener {

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event) {
        // we need to cancel all running contests in order to terminate
        // the timer thread gracefully
        SessionUtil.getInstance().getContestManager().cancelAllContests();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event) {
        SessionUtil.getInstance().initialize(event.getServletContext(),
                new XMLSerializerFactory(event.getServletContext().getRealPath("/judge/storage/")));
    }
}
