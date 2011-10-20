package plotter.connection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.directwebremoting.ScriptSession;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.event.ScriptSessionEvent;
import org.directwebremoting.event.ScriptSessionListener;
import org.directwebremoting.extend.ScriptSessionManager;

public class SessionTracker implements ScriptSessionListener {

	// Singleton Pattern instance
	private static SessionTracker instance = null;

	// Set with currently active Sessions
	private Set<ScriptSession> scriptSessionsList = new HashSet<ScriptSession>();
	private ScriptSessionManager manager;

	private Map<String, DocumentSessionTuple> activePrintJobs;


	public SessionTracker(Manager parent) {
		this.activePrintJobs = new HashMap<String, DocumentSessionTuple>();

		// adding first Session
		sessionCreated(new ScriptSessionEvent(WebContextFactory.get()
				.getScriptSession()));

		// get ScriptSessionManager
		manager = ServerContextFactory.get().getContainer()
				.getBean(ScriptSessionManager.class);

		// add listener for loading / unloading
		manager.addScriptSessionListener(this);
	}

	public static SessionTracker getInstance(Manager parent) {
		if (instance == null)
			instance = new SessionTracker(parent);
		return instance;
	}

	public Set<ScriptSession> getSessions() {
		return this.scriptSessionsList;
	}

	@Override
	public void sessionCreated(ScriptSessionEvent ev) {
		synchronized (scriptSessionsList) {
			scriptSessionsList.add(ev.getSession());
		}
	}

	@Override
	public void sessionDestroyed(ScriptSessionEvent ev) {
		removeSession(ev.getSession());
	}

	public void removeSession(ScriptSession ss) {
		synchronized (scriptSessionsList) {
			scriptSessionsList.remove(ss);
		}
	}

	public Map<String, DocumentSessionTuple> getActivePrintJobs() {
		return activePrintJobs;
	}
}
