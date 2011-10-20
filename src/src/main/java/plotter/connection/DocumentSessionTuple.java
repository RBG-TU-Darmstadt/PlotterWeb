package plotter.connection;

import org.directwebremoting.ScriptSession;

import plotter.entities.Document;

public class DocumentSessionTuple {

	private Document document;
	private ScriptSession scriptSession;

	public DocumentSessionTuple(Document document, ScriptSession scriptSession) {
		this.document = document;
		this.scriptSession = scriptSession;
	}

	/**
	 * @return the document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * @return the scriptSession
	 */
	public ScriptSession getScriptSession() {
		return scriptSession;
	}

	/**
	 * @param document
	 *            the document to set
	 */
	public void setDocument(Document document) {
		this.document = document;
	}

	/**
	 * @param scriptSession
	 *            the scriptSession to set
	 */
	public void setScriptSession(ScriptSession scriptSession) {
		this.scriptSession = scriptSession;
	}
}
