package plotter.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import plotter.util.Configuration;

/**
 * Initialization of configuration
 */
public class InitializeContext implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		// Load configuration
		InputStream is = contextEvent.getServletContext().getResourceAsStream("/WEB-INF/configuration.properties");

		if (is != null) {
			Configuration.loadProperties(is);
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new Error("Missing configuration file '/WEB-INF/configuration.properties'");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {}

}
