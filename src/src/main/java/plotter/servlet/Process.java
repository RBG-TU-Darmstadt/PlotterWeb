package plotter.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;

import plotter.pdf.PrintJob;
import plotter.pdf.Prices;
import plotter.storage.UserDAO;
import plotter.entities.User;

/**
 * Main interface servlet
 */
public class Process extends HttpServlet {

	private static final long serialVersionUID = 9194502702273320379L;

	/*
	 * Globals names for session variables
	 */
	public final static String sessionJobs = "plotterJobs";
	public final static String sessionTempJobs = "plotterTempJobs";
	public final static String sessionUser = "plotterUser";
	public final static String sessionPrincipal = "plotterPrincipal";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Process() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);

		// Attributes you get from TU-SSO:
		// {__AUTHUSERCONTEXT__=student, cn=simon_o, __AUTHTYPE__=TUID,
		// surname=Olberding, givenName=Simon, tudUserUniqueID=99374839}

		AttributePrincipal principal = (AttributePrincipal) request.getUserPrincipal();

		// Check user
		if (request.getUserPrincipal() == null
				|| ! ((String) principal.getAttributes().get("__AUTHUSERCONTEXT__")).equals("employee")) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);

			request.getRequestDispatcher("/WEB-INF/jsp/forbidden.jsp").forward(request, response);

			return;
		}

		// Retrieve user
		String tuid = (String) principal.getAttributes().get("tudUserUniqueID");
		User user = null;
		if(UserDAO.exists(tuid)) {
			user = UserDAO.get(tuid);
		} else {
			user = new User();
			user.setTuid(tuid);
			UserDAO.create(user);
		}

		// Save user and principal to session
		session.setAttribute(sessionPrincipal, principal);
		session.setAttribute(sessionUser, user);

		// Workaround for missing User on deactivated authentication
//		String tuid = "123456";
//
//		Map<String, Object> attributes = new HashMap<String, Object>();
//		attributes.put("tudUserUniqueID", tuid);
//		attributes.put("givenName", "Hans");
//		attributes.put("surname", "Peter");
//
//		AttributePrincipal principal = (AttributePrincipal) new AttributePrincipalImpl("blub", attributes);
//
//		User user = null;
//		if(UserDAO.exists(tuid)) {
//			user = UserDAO.get(tuid);
//		} else {
//			user = new User();
//			user.setTuid(tuid);
//			UserDAO.create(user);
//		}
//
//		session.setAttribute(sessionPrincipal, principal);
//		session.setAttribute(sessionUser, user);

		// Create temporary job storage
		Map<String, PrintJob> tempJobs = (LinkedHashMap<String, PrintJob>) session.getAttribute(sessionTempJobs);
		if(tempJobs == null) {
			tempJobs = new LinkedHashMap<String, PrintJob>();
			session.setAttribute(sessionTempJobs, tempJobs);
		}
		// Create print job storage
		List<PrintJob> jobs = (ArrayList<PrintJob>) session.getAttribute(sessionJobs);
		if(jobs == null) {
			jobs = new ArrayList<PrintJob>();
			session.setAttribute(sessionJobs, jobs);
		}

		// Hand over infos to page
		ProcessDAO processDAO = new ProcessDAO();
		processDAO.setFirstName((String) principal.getAttributes().get("givenName"));
		processDAO.setLastName((String) principal.getAttributes().get("surname"));
		processDAO.setFormats(Prices.getInstance().getPrices());
		request.setAttribute("processDAO", processDAO);

		request.getRequestDispatcher("/WEB-INF/jsp/upload.jsp").forward(request, response);
	}

}