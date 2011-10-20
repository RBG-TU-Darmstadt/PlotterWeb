package plotter.connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.EmailValidator;
import org.directwebremoting.ScriptBuffer;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.ScriptSessions;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.dwrp.CommonsFileUpload;
import org.directwebremoting.event.SessionProgressListener;
import org.directwebremoting.io.FileTransfer;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plotter.pdf.FormatException;
import plotter.pdf.PDFile;
import plotter.pdf.Prices;
import plotter.servlet.Process;
import plotter.storage.DocumentDAO;
import plotter.storage.UserDAO;
import plotter.entities.Document;
import plotter.entities.User;
import plotter.util.Export;
import plotter.util.PlotterUtil;

@RemoteProxy
public class Manager {

	private SessionTracker sessionTracker;
	protected List<ServerContext> serverContextList = null;

	public Manager() {
		sessionTracker = SessionTracker.getInstance(this);
	}

	@RemoteMethod
	public void register() {
		ServerContext serverContext = null;

		try {
			// Get server context, if necessary
			serverContext = ServerContextFactory.get();

			// Register function under current session id
			this.serverContextList.add(serverContext);
		} catch (Exception e) {
		}
	}

//	public void onMessage(final Message message) {
//		try {
//			if (message instanceof TextMessage) {
//				String text = ((TextMessage) message).getText();
//				// ScriptSessions.addFunctionCall("receiveMessage",
//				// text);
//				for (String jobKey : sessionTracker.getActivePrintJobs()
//						.keySet()) {
//					if (jobKey.equals(text)) {
//
//						Document doc = sessionTracker.getActivePrintJobs()
//								.get(jobKey).getDocument();
//						ScriptSession session = sessionTracker
//								.getActivePrintJobs().get(jobKey)
//								.getScriptSession();
//
//						// store in DB
//						DocumentDAO.create(doc);
//						UserDAO.update(doc.getUser());
//
//						// Send confimation e-mail
//						PlotterUtil.sendMail(doc);
//
//						session.addScript(new ScriptBuffer().appendCall(
//								"upload.receiveJobCallback", doc.toJSON()
//										.toString()));
//
//						sessionTracker.getActivePrintJobs().remove(jobKey);
//					}
//				}
//			}
//		} catch (Exception e) {
//			ScriptSessions.addFunctionCall("alert",
//					new Date() + ": " + e.toString());
//		}
//	}
//
//	public boolean sendFile(PDFile pdfFile) {
//		BlobMessage message;
//		ObjectMessage metaMessage;
//		try {
//			message = this.sessionTracker.getSession().createBlobMessage(
//					pdfFile);
//			sessionTracker.getProducer().send(message);
//
//			metaMessage = sessionTracker.getSession().createObjectMessage(
//					pdfFile.getMetadata());
//			sessionTracker.getProducer().send(metaMessage);
//		} catch (JMSException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
//
//	@Override
//	public void onException(JMSException arg0) {
//		// TODO Auto-generated method stub
//	}

	@RemoteMethod
	public String getJobs(HttpServletRequest request) throws JSONException {
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute(Process.sessionUser);
		List<Document> documents = DocumentDAO
				.getLastJobsFromUser(user.getId());

		JSONArray json = new JSONArray();
		for (Document document : documents) {
			json.put(document.toJSON());
		}

		return json.toString();
	}

	@RemoteMethod
	public String uploadFile(FileTransfer file, HttpServletRequest request)
			throws JSONException {
		if (file.getSize() == 0) {
			// File was empty
			return new JSONObject().put("success", false)
					.put("error", "file-empty").toString();
		}

		// Save file
		File tmp = null;
		try {
			tmp = File.createTempFile("plotter", ".pdf");
			tmp.deleteOnExit();

			FileOutputStream stream = new FileOutputStream(tmp);
			file.getOutputStreamLoader().load(stream);
			stream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Generate key
		String key = UUID.randomUUID().toString();

		// Instantiate PDF
		PDFile pdf = null;
		try {
			pdf = new PDFile(tmp.getAbsolutePath(), file.getFilename());
		} catch (IOException e) {
			e.printStackTrace();

			// File not valid
			return new JSONObject().put("success", false)
					.put("error", "file-not-valid").toString();
		}

		// save the jobKey in Metadata object
		pdf.getMetadata().setJobKey(key);

		List<String> images = new ArrayList<String>();
		for (int i = 0; i < pdf.getImages().size(); i++) {
			images.add(request.getContextPath() + "/secure/preview/?key=" + key
					+ "&num=" + i);
		}

		// Save to session
		HttpSession session = request.getSession(true);
		Map<String, PDFile> jobs = (LinkedHashMap<String, PDFile>) session
				.getAttribute(Process.sessionJobs);
		jobs.put(key, pdf);
		session.setAttribute(Process.sessionJobs, jobs);

		// Get mail
		String mail = ((User) session.getAttribute(Process.sessionUser))
				.getEmail();

		// Create JSON answer
		return new JSONObject().put("success", true).put("key", key)
				.put("pdf", pdf.toJSON()).put("images", images)
				.put("mail", mail).toString();
	}

	@RemoteMethod
	public String calculatePrice(String jobKey, String format, int copies,
			HttpServletRequest request) throws JSONException {
		// Get from session
		HttpSession session = request.getSession(true);
		Map<String, PDFile> jobs = (LinkedHashMap<String, PDFile>) session
				.getAttribute(Process.sessionJobs);
		PDFile pdf = jobs.get(jobKey);

		// Calculate price
		String formatedPrice = null;
		try {
			float price = Prices.getInstance().calculatePrice(
					pdf.getMetadata().getNumberOfPages(), copies, format);
			formatedPrice = String.format("%.2f", price);
		} catch (FormatException e1) {
			formatedPrice = "--";
		}

		// Create JSON answer
		return new JSONObject().put("key", pdf.getId())
				.put("price", formatedPrice).toString();
	}

	@RemoteMethod
	public String print(String jobKey, String format, int copies, String mail,
			HttpServletRequest request) throws JSONException {
		// Get from session
		HttpSession session = request.getSession(true);
		Map<String, PDFile> jobs = (LinkedHashMap<String, PDFile>) session
				.getAttribute(Process.sessionJobs);
		PDFile pdf = jobs.get(jobKey);

		if (!Prices.getInstance().getPrices().containsKey(format)) {
			// Invalid format
			return new JSONObject().put("success", false)
					.put("error", "format-not-valid").toString();
		}
		if (copies < 1) {
			// Invalid copies
			return new JSONObject().put("success", false)
					.put("error", "copies-not-valid").toString();
		}
		if (!EmailValidator.getInstance().isValid(mail)) {
			// Invalid mail
			return new JSONObject().put("success", false)
					.put("error", "mail-not-valid").toString();
		}

		// Set options
		pdf.getMetadata().setPrintSize(format);
		pdf.getMetadata().setCopies(copies);

		AttributePrincipal principal = (AttributePrincipal) session
				.getAttribute(Process.sessionPrincipal);
		User user = (User) session.getAttribute(Process.sessionUser);

		// Update user information
		user.setEmail(mail);
		user.setFirstName((String) principal.getAttributes().get("givenName"));
		user.setLastName((String) principal.getAttributes().get("surname"));

		// Calculate price
		Float price = -1.0f;
		try {
			price = Prices.getInstance().calculatePrice(
					pdf.getMetadata().getNumberOfPages(),
					pdf.getMetadata().getCopies(),
					pdf.getMetadata().getPrintSize());
		} catch (FormatException e) {
			System.out.println("Invalid format supplied for print");
		}

		Document doc = new Document(pdf.getFilename(), "", pdf.getMetadata()
				.getPrintSize(), pdf.getMetadata().getNumberOfPages()
				* pdf.getMetadata().getCopies(), pdf.getMetadata().getCopies(),
				pdf.getId(), price, user);
		doc.setPrintDate(new Date());

		// Print file
		// TODO: Implement new printing functionality
//		pdf.sendFileToPrinter();

		// save document and scriptsession
		sessionTracker.getActivePrintJobs().put(
				jobKey,
				new DocumentSessionTuple(doc, WebContextFactory.get()
						.getScriptSession()));

		// Remove job from session
		jobs.remove(jobKey);

		return new JSONObject().put("success", true).toString();
	}

	/**
	 * @param request
	 * @return
	 * @throws JSONException
	 */
	@RemoteMethod
	public String getDocuments(HttpServletRequest request) throws JSONException {
		List<Document> documents = DocumentDAO.getAllJobs();

		JSONArray json = new JSONArray();
		for (Document document : documents) {
			json.put(document.toJSON(true));
		}

		return json.toString();
	}

	@RemoteMethod
	public String getUploadStatus(HttpServletRequest request)
			throws JSONException {
		SessionProgressListener progressListener = (SessionProgressListener) request
				.getSession().getAttribute(CommonsFileUpload.PROGRESS_LISTENER);

		JSONObject json = new JSONObject();

		if (progressListener == null) {
			json.put("success", false);
		} else {
			json.put("success", true);
			json.put("progress", (double) progressListener.getBytesRead()
					/ (double) progressListener.getContentLength());

			if (progressListener.getBytesRead() == progressListener
					.getContentLength()) {
				// Delete progress listener
				request.getSession().removeAttribute(
						CommonsFileUpload.PROGRESS_LISTENER);
			}
		}

		return json.toString();
	}

	/**
	 * @param documentIds
	 * @param response
	 * @return
	 */
	@RemoteMethod
	public FileTransfer exportDocuments(List<Integer> documentIds,
			HttpServletResponse response) {

		byte[] ba = null;
		File xls = null;
		try {
			xls = Export.xlsExport(documentIds);
			ba = FileUtils.readFileToByteArray(xls);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return new FileTransfer(xls.getName(), "application/vnd.ms-excel", ba);
	}

	/**
	 * @param documentIds
	 * @param request
	 */
	@RemoteMethod
	public void removeDocuments(List<Integer> documentIds,
			HttpServletRequest request) {
		DocumentDAO.deleteJobs(documentIds);
	}
}
