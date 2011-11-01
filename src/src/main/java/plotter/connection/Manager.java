package plotter.connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.print.PrintException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.validator.EmailValidator;
import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.dwrp.CommonsFileUpload;
import org.directwebremoting.event.SessionProgressListener;
import org.directwebremoting.io.FileTransfer;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import plotter.entities.Document;
import plotter.entities.User;
import plotter.pdf.CopiesException;
import plotter.pdf.FormatException;
import plotter.pdf.PrintJob;
import plotter.pdf.PrintJobException;
import plotter.servlet.Process;
import plotter.storage.DocumentDAO;
import plotter.util.Export;

@RemoteProxy
public class Manager {

	protected List<ServerContext> serverContextList = null;

	public Manager() {}

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

	@RemoteMethod
	public String getJobs(HttpServletRequest request) throws JSONException {
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute(Process.sessionUser);

		JSONArray json = new JSONArray();

		// Add completed jobs
		List<Document> documents = DocumentDAO
				.getLastJobsFromUser(user.getId());
		for (Document document : documents) {
			json.put(document.toJSON());
		}

		// Add pending jobs
		@SuppressWarnings("unchecked")
		List<PrintJob> jobs = (ArrayList<PrintJob>) session.getAttribute(Process.sessionJobs);
		for (PrintJob job : jobs) {
			json.put(job.toJSON());
		}

		return json.toString();
	}

	@RemoteMethod
	public String uploadFile(FileTransfer file, HttpServletRequest request)
			throws JSONException {
		if (file.getSize() == 0) {
			// File was empty
			return new JSONObject()
					.put("success", false)
					.put("error", "file-empty")
				.toString();
		}

		// Save file
		File tmp;
		try {
			tmp = File.createTempFile("plotter", ".pdf");
			tmp.deleteOnExit();

			FileOutputStream stream = new FileOutputStream(tmp);
			file.getOutputStreamLoader().load(stream);
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();

			// Upload failed
			return new JSONObject()
					.put("success", false)
					.put("error", "upload-failed")
				.toString();
		}

		// Get user
		HttpSession session = request.getSession(true);
		User user = (User) session.getAttribute(Process.sessionUser);

		// Create temporary job
		PrintJob job;
		try {
			job = new PrintJob(tmp.getAbsolutePath(), file.getFilename(), session);

			// Generate thumbnails
			job.generateThumbnails();
		} catch (IOException e) {
			e.printStackTrace();

			// File not valid
			return new JSONObject()
					.put("success", false)
					.put("error", "file-not-valid")
				.toString();
		}

		// Generate key to identify the job
		String key = UUID.randomUUID().toString();

		List<String> images = new ArrayList<String>();
		for (int i = 0; i < job.getNumberOfPages(); i++) {
			images.add(request.getContextPath() + "/secure/preview/?key=" + key
					+ "&num=" + i);
		}

		// Save to session
		@SuppressWarnings("unchecked")
		Map<String, PrintJob> tempJobs = (LinkedHashMap<String, PrintJob>) session
				.getAttribute(Process.sessionTempJobs);
		tempJobs.put(key, job);

		// Create JSON answer
		return new JSONObject()
				.put("success", true)
				.put("key", key)
				.put("job", job.toJSON())
				.put("images", images)
				.put("mail", user.getEmail())
			.toString();
	}

	@RemoteMethod
	public String calculatePrice(String jobKey, String format, int copies,
			HttpServletRequest request) throws JSONException {
		// Get from session
		HttpSession session = request.getSession(true);
		@SuppressWarnings("unchecked")
		Map<String, PrintJob> tempJobs = (LinkedHashMap<String, PrintJob>) session
				.getAttribute(Process.sessionTempJobs);
		PrintJob job = tempJobs.get(jobKey);

		// Calculate price by updating job
		try {
			job.setPrintSize(format);
			job.setCopies(copies);
		} catch (PrintJobException e) {
			return new JSONObject()
					.put("success", false)
				.toString();
		}

		// Create JSON answer
		return new JSONObject()
				.put("success", true)
				.put("price", String.format("%.2f", job.getPrice()))
			.toString();
	}

	@RemoteMethod
	public String print(String jobKey, String format, int copies, String mail,
			HttpServletRequest request) throws JSONException {
		// Get from session
		HttpSession session = request.getSession(true);
		@SuppressWarnings("unchecked")
		Map<String, PrintJob> tempJobs = (LinkedHashMap<String, PrintJob>) session
				.getAttribute(Process.sessionTempJobs);
		PrintJob job = tempJobs.get(jobKey);

		/*
		 * Validate input
		 */
		try {
			// Set format
			job.setPrintSize(format);
		} catch (FormatException e1) {
			// Invalid format
			return new JSONObject()
					.put("success", false)
					.put("error", "format-not-valid")
				.toString();
		}
		try {
			// Set copies
			job.setCopies(copies);
		} catch (CopiesException e1) {
			// Invalid copies
			return new JSONObject()
					.put("success", false)
					.put("error", "copies-not-valid")
				.toString();
		}
		if ( ! EmailValidator.getInstance().isValid(mail)) {
			// Invalid mail
			return new JSONObject()
					.put("success", false)
					.put("error", "mail-not-valid")
				.toString();
		}

		AttributePrincipal principal = (AttributePrincipal) session
				.getAttribute(Process.sessionPrincipal);
		User user = (User) session.getAttribute(Process.sessionUser);

		// Update user information
		user.setEmail(mail);
		user.setFirstName((String) principal.getAttributes().get("givenName"));
		user.setLastName((String) principal.getAttributes().get("surname"));

		// Print file in background thread
		Thread printThread = new Thread(new PrintThread(session, job));
		printThread.start();

		// Remove temporary job from session
		tempJobs.remove(jobKey);

		return new JSONObject()
				.put("success", true)
			.toString();
	}

	class PrintThread implements Runnable {

		HttpSession session;
		PrintJob job;

		public PrintThread(HttpSession session, PrintJob job) {
			this.session = session;
			this.job = job;
		}

		@Override
		public void run() {
			try {
				job.print();

				// TODO send message to webinterface to reload jobs to show the new pending one
			} catch (PrintException e) {
				e.printStackTrace();

				job.finished(false);
			} catch (IOException e) {
				// TODO Send message to webinterface about failed print job (should probably show alert)
				e.printStackTrace();
			}
		}

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
